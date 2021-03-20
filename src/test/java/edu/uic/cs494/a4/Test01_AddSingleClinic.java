package edu.uic.cs494.a4;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test01_AddSingleClinic {
    @Test
    public void testMaxCapacity() {
        Lab l = Lab.createLab();
        int size = 10;
        int test = 20;
        Clinic c = l.createClinic(size);

        c.startThread();

        Set<VaccineDose> expected = new HashSet<>();

        for (int i = 0 ; i < test ; i++) {
            VaccineDose v = l.createVaccineDose(i);

            if (i < size) {
                expected.add(v);
                Assert.assertTrue(l.addVaccineDoses(c, Set.of(v)));
            } else {
                Assert.assertFalse(l.addVaccineDoses(c, Set.of(v)));
            }
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(expected, c.getReadyDoses());
    }

    @Test
    public void testMaxCapacityAsync() {
        Lab l = Lab.createLab();
        int size = 10;
        int test = 20;
        Clinic c = l.createClinic(size);

        c.startThread();

        Result<Boolean>[] rs = new Result[test];
        Set<VaccineDose> expected = new HashSet<>();

        for (int i = 0 ; i < test ; i++) {
            VaccineDose v = l.createVaccineDose(i);
            rs[i] = l.addVaccineDosesAsync(c, Set.of(v));
            if (i < size)
                expected.add(v);

        }

        for (int i = 0 ; i < test ; i++) {
            if (i < size)
                Assert.assertTrue(rs[i].getResult());
            else
                Assert.assertFalse(rs[i].getResult());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(expected, c.getReadyDoses());
    }

    @Test
    public void testVaccineDoseAlreadyInClinic() {
        int size = 10;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList);

        testVaccineDoseAlreadyInClinic(sequentialIndexes);
        testVaccineDoseAlreadyInClinic(shuffledIndexesList);
    }

    private void testVaccineDoseAlreadyInClinic(List<Integer> indexes) {
        int size = indexes.size();
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(size);

        c.startThread();

        VaccineDose[] vaccineDoses = new VaccineDose[size];
        for (int i = 0 ; i < size ; i++) {
            vaccineDoses[i] = l.createVaccineDose(i);
        }

        for (int i : indexes) {
            {
                Set<VaccineDose> toAdd = new HashSet<>();
                toAdd.add(vaccineDoses[i]);
                Assert.assertTrue(l.addVaccineDoses(c, toAdd));
                Assert.assertFalse(l.addVaccineDoses(c, toAdd));
                Assert.assertFalse(l.addVaccineDoses(c, Set.of(vaccineDoses[i])));
            }

            if (i > 0) {
                Set<VaccineDose> toAdd = new HashSet<>();
                toAdd.add(vaccineDoses[i]);
                toAdd.add(vaccineDoses[i-1]);
                Assert.assertFalse(l.addVaccineDoses(c, toAdd));
                Assert.assertFalse(l.addVaccineDoses(c, Set.of(vaccineDoses[i], vaccineDoses[i-1])));
            }

            {
                Set<VaccineDose> toAdd = new HashSet<>();
                toAdd.add(vaccineDoses[i]);
                VaccineDose anotherVaccineDose = l.createVaccineDose(size+i);
                toAdd.add(anotherVaccineDose);
                Assert.assertFalse(l.addVaccineDoses(c, toAdd));
                Assert.assertFalse(l.addVaccineDoses(c, Set.of(vaccineDoses[i], anotherVaccineDose)));
            }
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(vaccineDoses), c.getReadyDoses());
    }

    @Test
    public void testVaccineDoseAlreadyInClinicAsync() {
        int size = 10;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList);

        testVaccineDoseAlreadyInClinicAsync(sequentialIndexes);
        testVaccineDoseAlreadyInClinicAsync(shuffledIndexesList);
    }

    private void testVaccineDoseAlreadyInClinicAsync(List<Integer> indexes) {
        int size = indexes.size();
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(size);

        c.startThread();

        VaccineDose[] vaccineDoses = new VaccineDose[size];
        for (int i = 0 ; i < size ; i++) {
            vaccineDoses[i] = l.createVaccineDose(i);
        }

        List<Result<Boolean>> trueResults  = new LinkedList<>();
        List<Result<Boolean>> falseResults = new LinkedList<>();

        for (int i : indexes) {
            {
                Set<VaccineDose> toAdd = new HashSet<>();
                toAdd.add(vaccineDoses[i]);
                trueResults.add(l.addVaccineDosesAsync(c, toAdd));
                falseResults.add(l.addVaccineDosesAsync(c, toAdd));
                falseResults.add(l.addVaccineDosesAsync(c, Set.of(vaccineDoses[i])));
            }

            if (i > 0) {
                Set<VaccineDose> toAdd = new HashSet<>();
                toAdd.add(vaccineDoses[i]);
                toAdd.add(vaccineDoses[i-1]);
                falseResults.add(l.addVaccineDosesAsync(c, toAdd));
                falseResults.add(l.addVaccineDosesAsync(c, Set.of(vaccineDoses[i], vaccineDoses[i-1])));
            }

            {
                Set<VaccineDose> toAdd = new HashSet<>();
                toAdd.add(vaccineDoses[i]);
                VaccineDose anotherVaccineDose = l.createVaccineDose(size+i);
                toAdd.add(anotherVaccineDose);
                falseResults.add(l.addVaccineDosesAsync(c, toAdd));
                falseResults.add(l.addVaccineDosesAsync(c, Set.of(vaccineDoses[i], anotherVaccineDose)));
            }

        }

        for (Result<Boolean> r : trueResults)
            Assert.assertTrue(r.getResult());

        for (Result<Boolean> r : falseResults)
            Assert.assertFalse(r.getResult());

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(vaccineDoses), c.getReadyDoses());
    }


}
