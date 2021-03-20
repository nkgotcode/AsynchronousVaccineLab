package edu.uic.cs494.a4;

public interface VaccineDose {
    enum Status { READY, USED, DISCARDED }

    Status getStatus();
}
