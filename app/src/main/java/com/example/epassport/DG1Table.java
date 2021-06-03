package com.example.epassport;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Details recorder in MRZ (Машино читаемой зоне)
@Entity
public class DG1Table {
    @PrimaryKey
    @NonNull
    String id;
    // Document Details
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
    // Encoded faceshot hash
    String faceshotHash;
    // Signs
    String sign1;
    String sign2;
    // Chip id
    String chipId;
}
