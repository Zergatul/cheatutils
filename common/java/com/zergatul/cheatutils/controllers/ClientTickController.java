package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.Minecraft;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class ClientTickController {

    public static final ClientTickController instance = new ClientTickController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    private ClientTickController() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    public <T> T getResult(Supplier<T> supplier, int timeout) {
        if (mc.level == null) {
            return null;
        }

        SupplierQueueItem<T> item = new SupplierQueueItem<>(supplier);
        queue.add(item);

        try {
            synchronized (item) {
                item.wait(timeout);
            }
            return item.getResult();
        } catch (InterruptedException e) {
            queue.removeIf(i -> i == item);
            return null;
        }
    }

    private void onClientTickEnd() {
        while (queue.size() > 0) {
            Runnable item = queue.poll();
            item.run();
            synchronized (item) {
                item.notify();
            }
        }
    }

    private static class SupplierQueueItem<T> implements Runnable {

        private final Supplier<T> supplier;
        private volatile boolean finished;
        private volatile T result;

        public SupplierQueueItem(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public boolean isFinished() {
            return finished;
        }

        public T getResult() {
            return result;
        }

        @Override
        public void run() {
            result = supplier.get();
            finished = true;
        }
    }
}