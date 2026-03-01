package com.example.criminalintent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeRepository {
    private static CrimeRepository sInstance;

    private final List<Crime> crimes = new ArrayList<>();

    private CrimeRepository() { 
        // Add one solved crime
        Crime solvedCrime = new Crime(UUID.randomUUID(),
                "Crime #1", new Date(), true);
        crimes.add(solvedCrime);

        // Add one unsolved crime that requires police
        Crime unsolvedCrime = new Crime(UUID.randomUUID(),
                "Crime #2", new Date(), false);
        unsolvedCrime.setRequiresPolice(true);
        crimes.add(unsolvedCrime);
    }

    public static CrimeRepository get() {
        if (sInstance == null) {
            sInstance = new CrimeRepository();
        }
        return sInstance;
    }

    public List<Crime> getCrimes() {
        return crimes;
    }

    public Crime getCrime(UUID id) {
        if (id == null) return null;
        for (Crime crime : crimes) {
            if (id.equals(crime.getId())) {
                return crime;
            }
        }
        return null;
    }

    public void addCrime(Crime crime) {
        if (crime == null) return;
        crimes.add(0, crime);
    }
}
