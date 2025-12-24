# 3. Sözde Kod (Prototip Akışları)

Bu doküman, repodaki mevcut DigiBank prototipinin çekirdek akışlarını “ne oluyor?” seviyesinde özetler.

## 3.1 Login (SHA3-512 + Salt + TOTP + Backoff)

**Amaç:** `/api/login` çağrısında kullanıcıyı doğrula ve token üret.

```text
function POST /api/login(body)
    username = body.username
    password = body.password
    totp = body.totp

    if any missing -> 400
    user = userRepository.findByUsername(username)
    if user not found -> 401

    ok = authenticationService.login(user, password, totp)
    if ok == false -> 401

    token = randomUUID()
    tokenStore[token] = user
    return 200 { token }
end
```

`authenticationService.login(...)` içi:

```text
function login(user, password, totp)
    attempt = attempts[user.username]
    if attempt.isLocked() -> false

    passwordOk = SHA3_512(password + user.salt) == user.passwordHash (constant-time)
    totpOk = verifyTotp(user.totpSecret, totp)
        - DEMO secret ise "000000" kabul edilebilir
        - zaman kayması toleransı: current/prev/next window

    if passwordOk and totpOk
        attempt.reset()
        audit.log("login success")
        return true

    attempt.registerFailure()
    sleep(attempt.backoffMillis())
    return false
end
```

## 3.2 Token ile Yetkilendirme (ApiServer.requireAuth)

```text
function requireAuth(request)
    if REQUIRE_HTTPS=true
        if header X-Forwarded-Proto != https -> 400

    bearer = parse Authorization header
    if bearer in tokenStore -> return user

    if DEV_AUTH_TOKEN set and bearer == DEV_AUTH_TOKEN
        return userRepository.findByUsername("admin")

    return 401
end
```

## 3.3 Fatura Listeleme ve Ödeme (SmartGovernment + Strategy/Adapter)

**Fatura listeleme:**

```text
function GET /api/bills
    requireAuth + role RESIDENT/ADMIN
    bills = smartGovernmentService.fetchBills(mockCitizenId)
    return 200 bills
end
```

**Ödeme:**

```text
function POST /api/pay(body)
    requireAuth + role RESIDENT/ADMIN
    billId = body.billId
    payMode = body.payMode  // FIAT, BTC, ETH, STABLE
    if missing billId -> 400

    bills = smartGov.fetchBills(mockCitizenId)
    bill = find bills where billingId == billId
    if not found -> 404

    adapter = selectAdapter(payMode)
    if adapter exists
        ok = smartGov.payBillWithCrypto(user, bill, adapter)
    else
        ok = smartGov.payBill(user, bill)  // FiatPaymentStrategy

    if ok -> 200 {durum:BASARILI}
    else -> 402
end
```

`PaymentService` kayıt mantığı:

```text
function processPayment(user, amount, strategy)
    ok = strategy.pay(user, amount)
    tx = Transaction(id=nowMillis, userId=user.id, description="Odeme", amount=amount, status=ok?BASARILI:BASARISIZ)
    transactionRepository.save(tx)
    return ok
end

function processCryptoPayment(user, amountFiat, adapter)
    ok = adapter.pay(user, amountFiat)
    tx = Transaction(id=nowMillis, userId=user.id, description="KriptoOdeme-"+adapter.name, amount=amountFiat, status=...)
    transactionRepository.save(tx)
    return ok
end
```

## 3.4 Transactions CRUD (Admin) – “Manuel İşlem” Modeli

```text
function POST /api/transactions/create(body)
    requireAuth + role ADMIN

    if body contains fullName/bankCode/address/recordDate
        validate fullName, amount, bankCode, address
        recordDate = parse(recordDate) else today
        tx = Transaction(fullName, amount, bankCode, address, recordDate)
        created = transactionRepository.create(tx)
        return 201 created

    else
        // legacy model
        validate userId, description, amount
        created = transactionRepository.create(Transaction(userId, description, amount, status))
        return 201 created
end
```

## 3.5 Export (Admin) + Directory Watcher

```text
function POST /api/export
    requireAuth + role ADMIN
    ensure directory "txt" exists
    filename = "user_export_" + timestamp + ".txt"
    write file under txt/
    return 200 {durum:BASARILI, dosya:filename}
end

background thread DirectoryWatcherService("txt")
    watches ENTRY_CREATE
    on new file -> notifyObservers("NEW_FILE_DETECTED", filename)
    EmailNotificationObserver prints "email sent" (simulation)
end
```

## 3.6 GUI (Flask) Akışı (Özet)

```text
GET /login -> form
POST /login -> calls POST {API_URL}/api/login -> stores token in session

GET /smart-gov -> GET /api/bills -> POST /api/pay
GET /users -> GET /api/users or /api/users/search
POST /users -> POST/PUT/DELETE against /api/users/*
GET /transactions -> GET /api/transactions or /api/transactions/search
POST /transactions -> POST/PUT/DELETE against /api/transactions/*

POST /transactions/email -> build XLSX/PDF -> SMTP send to Mailpit
end
```
