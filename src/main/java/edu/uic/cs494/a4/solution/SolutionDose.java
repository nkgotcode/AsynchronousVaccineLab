package edu.uic.cs494.a4.solution;

import edu.uic.cs494.a4.VaccineDose;

public class SolutionDose implements VaccineDose {

    private final int id;
    Status status = Status.READY;

    public SolutionDose(int id) {
        this.id = id;
    }
    @Override
    public Status getStatus() {
        return this.status;
    }
}
