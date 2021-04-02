package edu.uic.cs494.a4.solution;

import edu.uic.cs494.a4.Result;

public class SolutionResult<T> extends Result<T> {
    @Override
    public void setResult(T result) {
        synchronized (this) {
            super.set(result);
            this.notifyAll();
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
                    }
                }
                return super.get();
            }
        }
    }
}
