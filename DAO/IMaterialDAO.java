package ums.dao;

import ums.model.Material;
import java.util.List;

public sealed interface IMaterialDAO permits MaterialDAOImpl {
    boolean        addMaterial(Material m);
    boolean        updateMaterial(Material m);     // update title, description, file path
    boolean        deleteMaterial(int materialId);
    List<Material> getByCourse(String C_code, String C_type);
    List<Material> getByLecturer(String lecId);
    Material       getById(int materialId);
}
