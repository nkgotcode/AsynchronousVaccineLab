package edu.uic.cs494.a4.solution;

import edu.uic.cs494.a4.Result;

import java.util.concurrent.atomic.AtomicBoolean;

public class SolutionResult<T> extends Result<T> {
    final private AtomicBoolean ready = new AtomicBoolean(false);
    @Override
    public void setResult(T result) {
        while (!ready.get()) {
            synchronized (this) {
                super.set(result);
                ready.compareAndSet(false, true);
                this.notifyAll();
            }
        }
    }

    @Override
    public T getResult() {
        while (true) {
            synchronized (this) {
                if (!this.isReady()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        continue;
                    }continue;
                }
            return super.get();
            }
        }
    }
}
