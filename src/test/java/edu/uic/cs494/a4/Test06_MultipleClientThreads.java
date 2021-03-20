package edu.uic.cs494.a4;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Test06_MultipleClientThreads {

    @Test
    public void testAdd() {
        Lab l = Lab.createLab();

        Clinic[] clinics = new Clinic[10];
        int size = 1_000;

        for (int i = 0 ; i < clinics.length ; i++) {
            clinics[i] = l.createClinic(size);
        }

        Thread[] threads = new Thread[10];
        Set<VaccineDose>[] allVaccineDoses = new Set[threads.length];

        for (int i = 0 ; i < threads.length ; i++) {
            int threadID = i;
            allVaccineDoses[i] = new HashSet<>();
            threads[i] = new Thread(() -> {
                Random rnd = new Random();
                for (int j = 0 ; j < size ; j++) {
                    VaccineDose vaccineDose = l.createVaccineDose(threadID*size + j);
                    Set<VaccineDose> vaccineDoses = Set.of(vaccineDose);
                    allVaccineDoses[threadID].add(vaccineDose);
                    while (true) {
                        // Pick a clinic at random
                        Clinic c = clinics[rnd.nextInt(clinics.length)];
                        // Clinic is full or add failed
                        if (l.getVaccineDoses(c).size() == size || !l.addVaccineDoses(c, vaccineDoses))
                            // Try again on another clinic
                            continue;
                        break;
                    }
                }
            });
        }

        AtomicBoolean exceptionThrown = startAllThreads(threads);

        for (Clinic c : clinics) {
            c.startThread();
        }

        joinAllThreads(threads);
        Assert.assertFalse(exceptionThrown.get());

        Assert.assertEquals(size*clinics.length, l.getVaccineDoses().size());

        Set<VaccineDose> expectedAllVaccineDoses = new HashSet<>();
        for (int i = 0 ; i < threads.length ; i++)
            expectedAllVaccineDoses.addAll(allVaccineDoses[i]);

        Assert.assertEquals(expectedAllVaccineDoses, l.getVaccineDoses());

        Set<VaccineDose> vaccineDosesOnClinics = new HashSet<>();
        for (int i = 0 ; i < clinics.length ; i++)
            vaccineDosesOnClinics.addAll(l.getVaccineDoses(clinics[i]));

        Assert.assertEquals(expectedAllVaccineDoses, vaccineDosesOnClinics);

        Set<VaccineDose> contents = new HashSet<>();
        for (Clinic c : clinics) {
            c.stopThread();

            Assert.assertFalse(c.didThrowException());
            contents.addAll(c.getReadyDoses());
        }

        Assert.assertEquals(expectedAllVaccineDoses, contents);
    }

    // Each thread (out of 10 threads) removes 100 vaccineDoses from lab with 1000 vaccineDoses
    @Test
    public void testRemove() {
        Lab l = Lab.createLab();

        Clinic[] clinics = new Clinic[10];
        int size = 100;

        for (int i = 0 ; i < clinics.length ; i++) {
            clinics[i] = l.createClinic(size);
            Set<VaccineDose> vaccineDoses = new HashSet<>();
            for (int j = 0 ; j < size ; j++) {
                VaccineDose vaccineDose = l.createVaccineDose(i*size + j);
                vaccineDoses.add(vaccineDose);
            }
            l.addVaccineDosesAsync(clinics[i], vaccineDoses);
        }

        Thread[] threads = new Thread[10];
        Set<VaccineDose>[] allVaccineDoses = new Set[threads.length];

        for (int i = 0 ; i < threads.length ; i++) {
            int threadID = i;
            allVaccineDoses[i] = new HashSet<>();
            threads[i] = new Thread(() -> {
                Random rnd = new Random();
                int removed = 0;
                while (removed < size) {
                    Clinic s = clinics[rnd.nextInt(clinics.length)];
                    // Get any vaccineDose
                    Optional<VaccineDose> vaccineDose = l.getVaccineDoses(s).stream().findAny();
                    if (!vaccineDose.isPresent())
                        continue;

                    boolean result;
                    if (removed%2 == 0)
                        result = l.administerVaccineDoses(s, Set.of(vaccineDose.get()));
                    else
                        result = l.discardVaccineDoses(s, Set.of(vaccineDose.get()));

                    if (result)
                        removed++;
                }
            });
        }

        AtomicBoolean exceptionThrown = startAllThreads(threads);

        for (Clinic c : clinics)
            c.startThread();

        joinAllThreads(threads);
        Assert.assertFalse(exceptionThrown.get());

        Assert.assertEquals(0, l.getVaccineDoses().size());
        Assert.assertEquals(Set.of(), l.getVaccineDoses());

        for (int i = 0 ; i < clinics.length ; i++)
            Assert.assertEquals(Set.of(), l.getVaccineDoses(clinics[i]));

        for (Clinic c : clinics) {
            c.stopThread();
            Assert.assertFalse(c.didThrowException());
            Assert.assertEquals(Set.of(), c.getReadyDoses());
        }

    }

    /*default*/ static AtomicBoolean startAllThreads(Thread[] threads) {

        AtomicBoolean exceptionThrown = new AtomicBoolean(false);

        // Uncaught exceptions cause tests to fail
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].setUncaughtExceptionHandler((t,ex) -> {
                System.err.println(ex.getMessage());
                ex.printStackTrace();
                exceptionThrown.set(true);
            });
        }

        // Start all threads
        for (int i = 0 ; i < threads.length ; i++)
            threads[i].start();

        return exceptionThrown;
    }

    /*default*/ static void joinAllThreads(Thread[] threads) {

        // Wait for all threads to finish
        for (int i = 0 ; i < threads.length ; i++) {
            while (threads[i].isAlive()) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
    }
}
