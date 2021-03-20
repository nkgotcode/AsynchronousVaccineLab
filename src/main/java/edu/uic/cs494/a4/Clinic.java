package edu.uic.cs494.a4;

import java.util.HashSet;
import java.util.Set;

public abstract class Clinic<D extends VaccineDose> implements Runnable {
    private final HashSet<D> readyDoses = new HashSet<>();
    protected final Thread allowedThread;
    private boolean exception = false;

    private final Action STOP = new Action(Action.Direction.CONTENTS, null, null);

    public Clinic() {
        this.allowedThread = new Thread(this);
        this.allowedThread.setDaemon(true);
        this.allowedThread.setUncaughtExceptionHandler( (Thread thread, Throwable throwable) -> {
            System.err.println(throwable.getMessage());
            throwable.printStackTrace();
            exception = true;
        });
    }

    public void startThread() {
        this.allowedThread.start();
    }

    public final void run() {
        while (true) {
            Action a = getAction();

            if (a == STOP)
                return;

            switch (a.getDirection()) {
                case ADD:
                    add((Set<D>) a.getTarget(), a.getResult());
                    break;
                case USE:
                    use((Set<D>) a.getTarget(), a.getResult());
                    break;
                case DISCARD:
                    discard((Set<D>) a.getTarget(), a.getResult());
                    break;
                case CONTENTS:
                    contents(a.getResult());
                    break;
                case REMOVE:
                    remove((Set<D>) a.getTarget(), a.getResult());
                    break;
                default:
                    throw new Error("Unknown operation");
            }
        }
    }

    public void addDoses(Set<D> doses) {
        if (this.allowedThread.isAlive() && Thread.currentThread() != this.allowedThread)
            throw new Error("Wrong thread!");

        this.readyDoses.addAll(doses);
    }

    public void removeDoses(Set<D> doses) {
        if (this.allowedThread.isAlive() && Thread.currentThread() != this.allowedThread)
            throw new Error("Wrong thread!");

        this.readyDoses.removeAll(doses);
    }

    public final Set<D> getReadyDoses() {
        if (this.allowedThread.isAlive() && Thread.currentThread() != this.allowedThread)
            throw new Error("Wrong thread!");

        return new HashSet<>(this.readyDoses);
    }

    public final void stopThread() {
        if (!this.allowedThread.isAlive())
            throw new Error("Thread already stopped, maybe due to an exception?");

        this.submitAction(STOP);

        while (this.allowedThread.isAlive()) {
            try {
                this.allowedThread.join();
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    public boolean didThrowException() {
        return this.exception;
    }

    public abstract void submitAction(Action a);

    protected abstract Action getAction();

    protected abstract void add(Set<D> doses, Result<Boolean> result);

    protected abstract void use(Set<D> doses, Result<Boolean> result);

    protected abstract void discard(Set<D> doses, Result<Boolean> result);

    protected abstract void contents(Result<Set<D>> result);

    protected abstract void remove(Set<D> doses, Result<Boolean> result);
}
