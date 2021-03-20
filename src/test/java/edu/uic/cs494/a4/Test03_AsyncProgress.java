package edu.uic.cs494.a4;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test03_AsyncProgress {

    @Test
    public void testMaxCapacityAsync() {
        Lab l = Lab.createLab();
        int size = 100;
        int test = 200;
        Clinic c = l.createClinic(size);


        Result<Boolean>[] rs = new Result[test];
        Set<VaccineDose> expected = new HashSet<>();

        for (int i = 0 ; i < test ; i++) {
            VaccineDose v = l.createVaccineDose(i);
            rs[i] = l.addVaccineDosesAsync(c, Set.of(v));
            if (i < size)
                expected.add(v);

        }

        c.startThread();

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
    public void testVaccineDoseAlreadyInClinicAsync() {
        int size = 1_000;
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

        c.startThread();

        for (Result<Boolean> r : trueResults)
            Assert.assertTrue(r.getResult());

        for (Result<Boolean> r : falseResults)
            Assert.assertFalse(r.getResult());

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(vaccineDoses), c.getReadyDoses());
    }

    @Test
    public void testCapacityAddAsync() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(1);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        Map<Result<Set<VaccineDose>>, Set<VaccineDose>> expected = new HashMap<>();

        {
            l.addVaccineDosesAsync(c, Set.of(v1, v2));

            expected.put(l.getVaccineDosesAsync(c), Set.of());
        }

        {
            l.addVaccineDosesAsync(c, Set.of(v1));
            l.addVaccineDosesAsync(c, Set.of(v2));

            expected.put(l.getVaccineDosesAsync(c), Set.of(v1));
        }

        c.startThread();

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(v1), c.getReadyDoses());
    }

    @Test
    public void testCapacitySameDescriptionAsync() {
        int size = 10;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList);

        testCapacitySameDescriptionAsync(sequentialIndexes);
        testCapacitySameDescriptionAsync(shuffledIndexesList);
    }

    public void testCapacitySameDescriptionAsync(List<Integer> indexes) {
        int size = indexes.size();
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(size);
        VaccineDose[] vaccineDoses = new VaccineDose[size];
        Set<VaccineDose> toAdd = new HashSet<>();
        Set<VaccineDose> expected = new HashSet<>();

        for (int i : indexes) {
            vaccineDoses[i] = l.createVaccineDose(i);
            toAdd.add(vaccineDoses[i]);
            expected.add(vaccineDoses[i]);
        }

        l.addVaccineDosesAsync(c, toAdd);

        c.startThread();

        Result<Set<VaccineDose>> r1 = l.getVaccineDosesAsync(c);

        Assert.assertEquals(size, r1.getResult().size());
        Assert.assertEquals(expected, r1.getResult());
        l.getVaccineDoses(c).clear();
        Assert.assertEquals(size, l.getVaccineDoses(c).size());
        Assert.assertEquals(expected, l.getVaccineDoses(c));

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(expected, c.getReadyDoses());
    }



}
