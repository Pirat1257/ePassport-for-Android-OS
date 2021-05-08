package com.example.epassport;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Blob;

// Details recorder in MRZ (Машино читаемой зоне)
@Entity
public class DG1Table {
    @PrimaryKey
    @NonNull
    int id;
    String documentType;
    String issuingState;
    String surname;
    String name;
    String documentNumber;
    String checkDigit_docNumber;
    String nationality;
    String dateOfBirth;
    String checkDigit_DOB;
    String sex;
    String dateOfIssue;
    String authority;
    String dateOfExpiryOrValidUntilDate;
    String checkDigit_DOE_VUD;
    String CompositeCheckDigit;
}
