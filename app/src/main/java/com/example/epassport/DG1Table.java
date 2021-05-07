package com.example.epassport;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Details recorder in MRZ (Машино читаемой зоне)
@Entity
public class DG1Table {
    String documentType;
    String issuingState;
    String name;
    @PrimaryKey
    @NonNull
    String documentNumber;
    String checkDigit_docNumber;
    String nationality;
    String dateOfBirth;
    String checkDigit_DOB;
    String sex;
    String dataOfExpiryOrValidUntilDate;
    String checkDigit_DOE_VUD;
    String CompositeCheckDigit;
}
