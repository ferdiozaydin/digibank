from flask import Flask, render_template, request, redirect, url_for, flash, session, send_file
import requests
import os
import io
from datetime import datetime

app = Flask(__name__)
app.secret_key = 'digibank_secret'

# Configuration
API_URL = os.environ.get('BACKEND_URL', 'http://localhost:8080')
VALID_USERNAME = os.environ.get('DIGIBANK_USERNAME', 'admin')
VALID_PASSWORD = os.environ.get('DIGIBANK_PASSWORD', 'admin')
VALID_TOTP = os.environ.get('DIGIBANK_TOTP', '000000')

# Mock data for UI demos
FAKE_CARDS = [
    {
        "id": "DIGI-001",
        "name": "DigiBank Black",
        "brand": "VISA",
        "limit": 50000,
        "available": 38250,
        "status": "Aktif",
        "lastTx": "Trendyol 1.250,00₺",
    },
    {
        "id": "DIGI-002",
        "name": "DigiBank Seyahat",
        "brand": "Mastercard",
        "limit": 20000,
        "available": 7800,
        "status": "Temassız Kapalı",
        "lastTx": "İGA Lounge 450,00₺",
    },
    {
        "id": "DIGI-003",
        "name": "DigiBank Sanal",
        "brand": "VISA",
        "limit": 5000,
        "available": 4900,
        "status": "Aktif",
        "lastTx": "App Store 89,99₺",
    },
]

FAKE_CARD_TX = [
    {"desc": "Market Harcaması", "amount": -320.75, "date": "12.01 14:21"},
    {"desc": "QR Para Çekme", "amount": -1500.0, "date": "12.01 10:04"},
    {"desc": "Kripto Satın Alma", "amount": -250.0, "date": "11.30 20:10"},
    {"desc": "Para İadesi", "amount": 420.0, "date": "11.30 08:15"},
]

DEFAULT_PREFERENCES = {
    "biometric": True,
    "push": True,
    "spendAlerts": True,
    "travelMode": False,
    "darkMode": True,
}

DEFAULT_METRICS = {
    "apiLatencyMs": 120,
    "paymentSuccessRate": 98.5,
    "sensorAlertsActive": 3,
    "cryptoUsagePct": 42,
}

# Basit zaman serisi demo verisi (dashboard'a ek performans sayfası için)
DEFAULT_METRIC_TIMELINE = [
    {"label": "09:00", "latency": 148, "success": 97.8, "throughput": 162, "queue": 3},
    {"label": "10:00", "latency": 131, "success": 98.2, "throughput": 175, "queue": 2},
    {"label": "11:00", "latency": 118, "success": 98.6, "throughput": 184, "queue": 2},
    {"label": "12:00", "latency": 126, "success": 98.0, "throughput": 191, "queue": 3},
    {"label": "13:00", "latency": 112, "success": 98.9, "throughput": 205, "queue": 1},
    {"label": "14:00", "latency": 107, "success": 99.1, "throughput": 212, "queue": 1},
    {"label": "15:00", "latency": 115, "success": 98.7, "throughput": 208, "queue": 2},
]

FX_RATES = {
    "USD": {"TRY": 31.2, "EUR": 0.92, "GBP": 0.79},
    "EUR": {"TRY": 34.1, "USD": 1.08, "GBP": 0.86},
    "GBP": {"TRY": 39.5, "USD": 1.26, "EUR": 1.17},
    "TRY": {"USD": 0.032, "EUR": 0.029, "GBP": 0.025},
}

DEMO_ACCOUNTS = [
    {"iban": "TR12 0001 0000 1234 5678 0001", "name": "Vadesiz TL", "balance": 18500.75},
    {"iban": "TR34 0001 0000 1234 5678 0002", "name": "USD Hesabı", "balance": 5200.00},
    {"iban": "TR56 0001 0000 1234 5678 0003", "name": "EUR Hesabı", "balance": 3100.50},
]


def _normalize_user(raw: dict) -> dict:
    """Temel kullanici sozluk yapisini sabitle ve bos alanlari doldur."""
    return {
        "username": session.get("username") or raw.get("username", "Bağlantı yok"),
        "fiatBalance": raw.get("fiatBalance", 0),
        "cryptoBalance": raw.get("cryptoBalance", 0),
        "transactions": raw.get("transactions", []),
    }


def _is_authenticated() -> bool:
    return bool(session.get('logged_in'))


def _auth_headers():
    token = session.get('api_token')
    if token:
        return {"Authorization": f"Bearer {token}"}
    return {}


def _get_preferences() -> dict:
    prefs = session.get('preferences') or {}
    merged = DEFAULT_PREFERENCES.copy()
    merged.update({k: bool(v) for k, v in prefs.items()})
    return merged


def _append_card_event(card_id: str, action: str):
    events = session.get('card_events') or []
    events.insert(0, {
        "cardId": card_id or "Bilinmiyor",
        "action": action or "İşlem",
        "ts": datetime.now().strftime("%d.%m %H:%M")
    })
    session['card_events'] = events[:12]


def _get_fx_rate(src: str, dst: str):
    if not src or not dst:
        return None
    if src == dst:
        return 1.0
    return FX_RATES.get(src, {}).get(dst)


@app.before_request
def require_login():
    allowed_endpoints = {'login', 'static'}
    if request.endpoint in allowed_endpoints or request.endpoint is None:
        return

    if not _is_authenticated():
        flash('Lütfen giriş yapınız.', 'error')
        return redirect(url_for('login', next=request.path))


@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username', '').strip()
        password = request.form.get('password', '')
        totp = request.form.get('totp', '').strip() or VALID_TOTP

        if username == VALID_USERNAME and password == VALID_PASSWORD:
            # Request backend token with MFA code
            try:
                resp = requests.post(
                    f"{API_URL}/api/login",
                    json={"username": username, "password": password, "totp": totp},
                    timeout=5,
                )
                data = resp.json() if resp.status_code == 200 else {}
                token = data.get('token')
                if not token:
                    raise ValueError('API token alınamadı')
                session['api_token'] = token
            except Exception as e:
                flash(f'API oturum açılamadı: {e}', 'error')
                return render_template(
                    'login.html',
                    user=_normalize_user({}),
                    default_username=VALID_USERNAME,
                    default_password=VALID_PASSWORD,
                    default_totp=VALID_TOTP,
                )

            session['logged_in'] = True
            session['username'] = username
            flash('Giriş başarılı.', 'success')
            next_url = request.args.get('next')
            return redirect(next_url or url_for('dashboard'))

        flash('Geçersiz kullanıcı adı veya şifre.', 'error')

    return render_template(
        'login.html',
        user=_normalize_user({}),
        default_username=VALID_USERNAME,
        default_password=VALID_PASSWORD,
        default_totp=VALID_TOTP,
    )


@app.route('/logout')
def logout():
    session.clear()
    flash('Oturum kapatıldı.', 'success')
    return redirect(url_for('login'))

@app.route('/')
def dashboard():
    # Fetch User Data from Java Backend
    try:
        resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = resp.json() if resp.status_code == 200 else {}
    except Exception:
        user_data = {}

    try:
        metrics_resp = requests.get(f"{API_URL}/api/metrics", timeout=3, headers=_auth_headers())
        metrics = metrics_resp.json() if metrics_resp.status_code == 200 else {}
    except Exception:
        metrics = {}

    merged_metrics = {**DEFAULT_METRICS, **{k: v for k, v in (metrics or {}).items() if v is not None}}

    return render_template(
        'dashboard.html',
        user=_normalize_user(user_data),
        metrics=merged_metrics,
        metrics_live=bool(metrics),
    )


@app.route('/performance')
def performance():
    # Kullanıcıyı çek (başlıkta isim gözüksün)
    try:
        user_resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = user_resp.json() if user_resp.status_code == 200 else {}
    except Exception:
        user_data = {}

    # Canlı metrikleri dene, yoksa demo verisine düş
    try:
        metrics_resp = requests.get(f"{API_URL}/api/metrics", timeout=3, headers=_auth_headers())
        metrics = metrics_resp.json() if metrics_resp.status_code == 200 else {}
    except Exception:
        metrics = {}

    merged_metrics = {**DEFAULT_METRICS, **{k: v for k, v in (metrics or {}).items() if v is not None}}

    # Zaman serisi verisi: canlı metrik varsa son noktayı ekle
    timeline = [dict(row) for row in DEFAULT_METRIC_TIMELINE]
    if merged_metrics:
        timeline.append({
            "label": datetime.now().strftime("%H:%M"),
            "latency": merged_metrics.get("apiLatencyMs", DEFAULT_METRICS["apiLatencyMs"]),
            "success": merged_metrics.get("paymentSuccessRate", DEFAULT_METRICS["paymentSuccessRate"]),
            "throughput": merged_metrics.get("transactionsPerMin", 210),
            "queue": merged_metrics.get("queueDepth", 2),
        })

    return render_template(
        'performance.html',
        user=_normalize_user(user_data),
        metrics=merged_metrics,
        metrics_live=bool(metrics),
        timeline=timeline,
    )


@app.route('/cards', methods=['GET', 'POST'])
def cards():
    if request.method == 'POST':
        action = request.form.get('action', '').strip() or 'İşlem'
        card_id = request.form.get('cardId', '').strip() or 'Kart'
        _append_card_event(card_id, action)
        flash(f"{card_id} için '{action}' isteği oluşturuldu (simülasyon).", 'success')
        return redirect(url_for('cards'))

    try:
        resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = resp.json() if resp.status_code == 200 else {}
    except Exception:
        user_data = {}

    return render_template(
        'cards.html',
        user=_normalize_user(user_data),
        cards=FAKE_CARDS,
        tx=FAKE_CARD_TX,
        events=session.get('card_events', []),
    )


@app.route('/home-control')
def home_control():
    # We can reuse user fetch for greeting
    try:
        resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = resp.json() if resp.status_code == 200 else {}
    except Exception:
        user_data = {}
    return render_template('home_control.html', user=_normalize_user(user_data))


@app.route('/home/toggle', methods=['POST'])
def home_toggle():
    device_id = request.form.get('deviceId', '').strip()
    state = request.form.get('state', 'off')
    on = state.lower() in ('on', 'true', '1')
    try:
        resp = requests.post(
            f"{API_URL}/api/home/toggle",
            json={"deviceId": device_id, "on": str(on).lower()},
            timeout=5,
            headers=_auth_headers(),
        )
        if resp.status_code == 200:
            flash(f"{device_id} için durum güncellendi", 'success')
        else:
            flash(resp.json().get('hata', 'Cihaz kontrolü başarısız'), 'error')
    except Exception:
        flash('Cihaz kontrolü başarısız: bağlantı hatası', 'error')
    return redirect(url_for('home_control'))


@app.route('/home/thermostat', methods=['POST'])
def home_thermostat():
    zone = request.form.get('zone', '').strip()
    target = request.form.get('target', '').strip()
    try:
        resp = requests.post(
            f"{API_URL}/api/home/thermostat",
            json={"zone": zone, "target": target},
            timeout=5,
            headers=_auth_headers(),
        )
        if resp.status_code == 200:
            flash(f"{zone} için hedef sıcaklık ayarlandı", 'success')
        else:
            flash(resp.json().get('hata', 'Termostat ayarı başarısız'), 'error')
    except Exception:
        flash('Termostat ayarı başarısız: bağlantı hatası', 'error')
    return redirect(url_for('home_control'))

@app.route('/smart-gov')
def smart_gov():
    try:
        user_resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = user_resp.json() if user_resp.status_code == 200 else {}

        bills_resp = requests.get(f"{API_URL}/api/bills", timeout=3, headers=_auth_headers())
        bills_data = bills_resp.json() if bills_resp.status_code == 200 else []
    except Exception as e:
        print(e)
        user_data = {}
        bills_data = []

    return render_template('smart_gov.html', bills=bills_data, user=_normalize_user(user_data))


@app.route('/fx', methods=['GET', 'POST'])
def fx_swap():
    try:
        resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = resp.json() if resp.status_code == 200 else {}
    except Exception:
        user_data = {}

    swap_result = None
    if request.method == 'POST':
        src = request.form.get('fromCurrency', 'USD').strip().upper()
        dst = request.form.get('toCurrency', 'TRY').strip().upper()
        try:
            amount = float(request.form.get('amount', 0) or 0)
        except ValueError:
            amount = 0
        rate = _get_fx_rate(src, dst)
        if rate:
            converted = round(amount * rate, 2)
            swap_result = {
                'src': src,
                'dst': dst,
                'amount': amount,
                'rate': rate,
                'converted': converted,
            }
            flash(f"{amount} {src} -> {converted} {dst} işlemi hazır (demo).", 'success')
        else:
            flash('Kur bulunamadı, lütfen farklı bir çift seçin.', 'error')

    return render_template(
        'fx_swap.html',
        user=_normalize_user(user_data),
        rates=FX_RATES,
        swap_result=swap_result,
    )


@app.route('/transfer', methods=['GET', 'POST'])
def transfer():
    try:
        resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = resp.json() if resp.status_code == 200 else {}
    except Exception:
        user_data = {}

    if request.method == 'POST':
        to_name = request.form.get('toName', 'Alıcı').strip() or 'Alıcı'
        iban = request.form.get('iban', '').strip()
        amount = request.form.get('amount', '').strip()
        desc = request.form.get('description', '').strip()
        try:
            resp = requests.post(
                f"{API_URL}/api/transfer",
                json={"toName": to_name, "iban": iban, "amount": str(amount), "description": desc},
                timeout=5,
                headers=_auth_headers(),
            )
            if resp.status_code == 201:
                flash(f"{to_name} adına {amount} TL transfer kaydı oluşturuldu.", 'success')
            else:
                msg = resp.json().get('hata', 'Transfer başarısız') if resp.content else 'Transfer başarısız'
                flash(msg, 'error')
        except Exception:
            flash('Transfer başarısız: bağlantı hatası', 'error')
        return redirect(url_for('transfer'))

    return render_template(
        'transfer.html',
        user=_normalize_user(user_data),
        accounts=DEMO_ACCOUNTS,
    )

@app.route('/pay/<bill_id>', methods=['POST'])
def pay_bill(bill_id):
    pay_mode = request.form.get('payMode', 'FIAT')
    try:
        resp = requests.post(
            f"{API_URL}/api/pay",
            json={"billId": bill_id, "payMode": pay_mode},
            timeout=5,
            headers=_auth_headers(),
        )
        try:
            data = resp.json()
        except ValueError:
            data = {}

        if resp.status_code == 200 and data.get('durum') == 'BASARILI':
            flash(f"{bill_id} numaralı fatura ödendi", 'success')
        else:
            flash(data.get('hata') or 'Ödeme başarısız: API hatası', 'error')
    except Exception:
         flash('Ödeme başarısız: Bağlantı hatası', 'error')
    
    return redirect(url_for('smart_gov'))

@app.route('/export', methods=['POST'])
def export_data():
    try:
        resp = requests.post(f"{API_URL}/api/export", timeout=5, headers=_auth_headers())
        if resp.status_code == 200:
            data = resp.json()
            flash(f"Dışarı aktarma başarılı! Oluşan dosya: {data.get('dosya')}", "success")
        else:
            flash("Dışarı aktarma başarısız.", "error")
    except Exception as e:
        flash("Dışarı aktarma hatası: Bağlantı sorunu", "error")
    
    return redirect(url_for('dashboard'))


@app.route('/export/download')
def export_download():
    """Basit CSV çıktı ile istemci tarafında indirilebilir içerik üretir."""
    try:
        resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = resp.json() if resp.status_code == 200 else {}
    except Exception:
        user_data = {}

    user = _normalize_user(user_data)
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    csv_lines = [
        "exported_at,username,fiat_balance,crypto_balance",
        f"{now},{user['username']},{user['fiatBalance']},{user['cryptoBalance']}",
    ]
    csv_content = "\n".join(csv_lines)
    file_buf = io.BytesIO(csv_content.encode("utf-8"))
    filename = f"digibank_user_export_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
    return send_file(file_buf, as_attachment=True, download_name=filename, mimetype='text/csv')


@app.route('/settings', methods=['GET', 'POST'])
def settings():
    prefs = _get_preferences()

    if request.method == 'POST':
        prefs['biometric'] = bool(request.form.get('biometric'))
        prefs['push'] = bool(request.form.get('push'))
        prefs['spendAlerts'] = bool(request.form.get('spendAlerts'))
        prefs['travelMode'] = bool(request.form.get('travelMode'))
        prefs['darkMode'] = bool(request.form.get('darkMode'))
        session['preferences'] = prefs

        logs = session.get('settings_logs') or []
        logs.insert(0, {
            "ts": datetime.now().strftime("%d.%m %H:%M"),
            "note": f"Ayarlar güncellendi: push={'açık' if prefs['push'] else 'kapalı'}, travel={'açık' if prefs['travelMode'] else 'kapalı'}",
        })
        session['settings_logs'] = logs[:10]

        flash('Ayarlar kaydedildi (simülasyon).', 'success')
        return redirect(url_for('settings'))

    try:
        resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = resp.json() if resp.status_code == 200 else {}
    except Exception:
        user_data = {}

    return render_template(
        'settings.html',
        user=_normalize_user(user_data),
        preferences=prefs,
        logs=session.get('settings_logs', []),
    )


@app.route('/users', methods=['GET', 'POST'])
def users():
    if request.method == 'POST':
        action = (request.form.get('action') or 'create').strip().lower()

        if action == 'create':
            new_username = request.form.get('username', '').strip()
            new_password = request.form.get('password', '').strip()
            role = request.form.get('role', 'RESIDENT').strip().upper() or 'RESIDENT'
            if not new_username or not new_password:
                flash('Kullanıcı adı ve şifre gerekli.', 'error')
            else:
                try:
                    resp = requests.post(
                        f"{API_URL}/api/users/register",
                        json={"username": new_username, "password": new_password, "role": role},
                        headers=_auth_headers(),
                        timeout=5,
                    )
                    if resp.status_code == 201:
                        flash(f"{new_username} eklendi.", 'success')
                    else:
                        msg = resp.json().get('hata', 'Kayıt başarısız') if resp.content else 'Kayıt başarısız'
                        flash(msg, 'error')
                except Exception:
                    flash('Kayıt başarısız: bağlantı hatası', 'error')
            return redirect(url_for('users'))

        if action == 'update':
            username = request.form.get('edit_username', '').strip()
            role = request.form.get('edit_role', '').strip().upper()
            fiat = (request.form.get('edit_fiat') or '').strip()
            crypto = (request.form.get('edit_crypto') or '').strip()
            try:
                resp = requests.put(
                    f"{API_URL}/api/users/item",
                    params={"username": username},
                    json={"role": role, "fiatBalance": fiat, "cryptoBalance": crypto},
                    headers=_auth_headers(),
                    timeout=5,
                )
                if resp.status_code == 200:
                    flash(f"{username} güncellendi.", 'success')
                else:
                    msg = resp.json().get('hata', 'Güncelleme başarısız') if resp.content else 'Güncelleme başarısız'
                    flash(msg, 'error')
            except Exception:
                flash('Güncelleme başarısız: bağlantı hatası', 'error')
            return redirect(url_for('users'))

        if action == 'delete':
            username = request.form.get('delete_username', '').strip()
            try:
                resp = requests.delete(
                    f"{API_URL}/api/users/item",
                    params={"username": username},
                    headers=_auth_headers(),
                    timeout=5,
                )
                if resp.status_code == 200:
                    flash(f"{username} silindi.", 'success')
                else:
                    msg = resp.json().get('hata', 'Silme başarısız') if resp.content else 'Silme başarısız'
                    flash(msg, 'error')
            except Exception:
                flash('Silme başarısız: bağlantı hatası', 'error')
            return redirect(url_for('users'))

        flash('Bilinmeyen işlem.', 'error')
        return redirect(url_for('users'))

    try:
        q = (request.args.get('q') or '').strip()
        if q:
            list_resp = requests.get(
                f"{API_URL}/api/users/search",
                params={"q": q},
                headers=_auth_headers(),
                timeout=5,
            )
        else:
            list_resp = requests.get(f"{API_URL}/api/users", headers=_auth_headers(), timeout=5)
        users_data = list_resp.json() if list_resp.status_code == 200 else []
    except Exception:
        users_data = []

    try:
        resp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = resp.json() if resp.status_code == 200 else {}
    except Exception:
        user_data = {}

    return render_template('users.html', user=_normalize_user(user_data), users=users_data, q=(request.args.get('q') or '').strip())


@app.route('/transactions', methods=['GET', 'POST'])
def transactions():
    if request.method == 'POST':
        action = (request.form.get('action') or 'create').strip().lower()

        if action == 'create':
            user_id = (request.form.get('userId') or '').strip()
            desc = (request.form.get('description') or '').strip()
            amount = (request.form.get('amount') or '').strip()
            status = (request.form.get('status') or 'BASARILI').strip().upper() or 'BASARILI'
            try:
                resp = requests.post(
                    f"{API_URL}/api/transactions/create",
                    json={"userId": str(user_id), "description": desc, "amount": str(amount), "status": status},
                    headers=_auth_headers(),
                    timeout=5,
                )
                if resp.status_code == 201:
                    flash('İşlem kaydı eklendi.', 'success')
                else:
                    msg = resp.json().get('hata', 'Kayıt başarısız') if resp.content else 'Kayıt başarısız'
                    flash(msg, 'error')
            except Exception:
                flash('Kayıt başarısız: bağlantı hatası', 'error')
            return redirect(url_for('transactions'))

        if action == 'update':
            tx_id = (request.form.get('edit_id') or '').strip()
            desc = (request.form.get('edit_description') or '').strip()
            amount = (request.form.get('edit_amount') or '').strip()
            status = (request.form.get('edit_status') or '').strip().upper()
            try:
                resp = requests.put(
                    f"{API_URL}/api/transactions/item",
                    params={"id": tx_id},
                    json={"description": desc, "amount": str(amount), "status": status},
                    headers=_auth_headers(),
                    timeout=5,
                )
                if resp.status_code == 200:
                    flash('İşlem güncellendi.', 'success')
                else:
                    msg = resp.json().get('hata', 'Güncelleme başarısız') if resp.content else 'Güncelleme başarısız'
                    flash(msg, 'error')
            except Exception:
                flash('Güncelleme başarısız: bağlantı hatası', 'error')
            return redirect(url_for('transactions'))

        if action == 'delete':
            tx_id = (request.form.get('delete_id') or '').strip()
            try:
                resp = requests.delete(
                    f"{API_URL}/api/transactions/item",
                    params={"id": tx_id},
                    headers=_auth_headers(),
                    timeout=5,
                )
                if resp.status_code == 200:
                    flash('İşlem silindi.', 'success')
                else:
                    msg = resp.json().get('hata', 'Silme başarısız') if resp.content else 'Silme başarısız'
                    flash(msg, 'error')
            except Exception:
                flash('Silme başarısız: bağlantı hatası', 'error')
            return redirect(url_for('transactions'))

        flash('Bilinmeyen işlem.', 'error')
        return redirect(url_for('transactions'))

    # GET: list/search
    try:
        q = (request.args.get('q') or '').strip()
        if q:
            resp = requests.get(
                f"{API_URL}/api/transactions/search",
                params={"q": q},
                headers=_auth_headers(),
                timeout=5,
            )
        else:
            resp = requests.get(f"{API_URL}/api/transactions", headers=_auth_headers(), timeout=5)
        txs = resp.json() if resp.status_code == 200 else []
    except Exception:
        txs = []

    try:
        uresp = requests.get(f"{API_URL}/api/user", timeout=3, headers=_auth_headers())
        user_data = uresp.json() if uresp.status_code == 200 else {}
    except Exception:
        user_data = {}

    return render_template('transactions.html', user=_normalize_user(user_data), txs=txs, q=(request.args.get('q') or '').strip())

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8000)
