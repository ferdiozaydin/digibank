package com.digibank.patterns.observer;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class DirectoryWatcherService implements Runnable {
    private List<Observer> observers = new ArrayList<>();
    private Path path;
    private WatchService watchService;
    private boolean running = true;

    public DirectoryWatcherService(String dirPath) throws IOException {
        this.path = Paths.get(dirPath);
        if (!Files.exists(this.path)) {
            Files.createDirectories(this.path);
        }
        this.watchService = FileSystems.getDefault().newWatchService();
        this.path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String eventType, String location) {
        for (Observer observer : observers) {
            observer.update(eventType, location);
        }
    }

    @Override
    public void run() {
        System.out.println("Klasor izleme servisi basladi: " + path.toAbsolutePath());
        try {
            while (running) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException x) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    notifyObservers("NEW_FILE_DETECTED", filename.toString());
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
