package edu.uic.cs494.a4;

import java.util.Set;

public abstract class Lab<C extends Clinic, V extends VaccineDose> {
    public static Lab<?, ?> createLab() {
        throw new Error("Not implemented");
    }

    public abstract C createClinic(int capacity);

    public abstract V createVaccineDose(int id);

    public abstract boolean addVaccineDoses(C clinic, Set<V> vaccineDoses);

    public abstract boolean administerVaccineDoses(C clinic, Set<V> vaccineDoses);

    public abstract boolean discardVaccineDoses(C clinic, Set<V> vaccineDoses);

    public abstract boolean moveVaccineDoses(C from, C to, Set<V> vaccineDoses);

    public abstract Set<V> getVaccineDoses();

    public abstract Set<V> getVaccineDoses(C clinic);

    public abstract Result<Boolean> addVaccineDosesAsync(C clinic, Set<V> vaccineDoses);

    public abstract Result<Boolean> administerVaccineDosesAsync(C clinic, Set<V> vaccineDoses);

    public abstract Result<Boolean> discardVaccineDosesAsync(C clinic, Set<V> vaccineDoses);

    public abstract Result<Boolean> moveVaccineDosesAsync(C from, C to, Set<V> vaccineDoses);

    public abstract Result<Set<V>> getVaccineDosesAsync();

    public abstract Result<Set<V>> getVaccineDosesAsync(C clinic);
}