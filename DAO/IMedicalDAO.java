package ums.dao;

import ums.model.MedicalRecord;
import java.util.List;

public sealed interface IMedicalDAO permits MedicalDAOImpl {
    boolean addMedical(MedicalRecord r);
    boolean updateStatus(String medId, String type, String status);
    List<MedicalRecord> getByStudent(String studentId, String C_code, String C_type, String Medi_type);
    List<MedicalRecord> getAll(String Medi_type);
    boolean deleteById(String medId, String Medi_type);
}
