package com.digibank.patterns.command;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Simple invoker to queue and execute commands.
 */
public class CommandInvoker {
    private final Queue<Command> queue = new LinkedList<>();

    public void submit(Command command) {
        queue.add(command);
    }

    public void runAll() {
        while (!queue.isEmpty()) {
            Command c = queue.poll();
            c.execute();
        }
    }
}
