package mx.edu.unpa.calificacionesunpa.models;

import java.util.List;

public class StudentBasic {
    private final String nombre;
    private final String apePaterno;
    private final String apeMaterno;
    private final String correo;
    private final String matricula;
    private final boolean activo;
    private final List<String> materias;

    public StudentBasic(String nombre,
                        String apePaterno,
                        String apeMaterno,
                        String correo,
                        String matricula,
                        boolean activo,
                        List<String> materias) {
        this.nombre = nombre;
        this.apePaterno = apePaterno;
        this.apeMaterno = apeMaterno;
        this.correo = correo;
        this.matricula = matricula;
        this.activo = activo;
        this.materias = materias;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApePaterno() {
        return apePaterno;
    }

    public String getApeMaterno() {
        return apeMaterno;
    }

    public String getCorreo() {
        return correo;
    }

    public String getMatricula() {
        return matricula;
    }

    public boolean isActivo() {
        return activo;
    }

    public List<String> getMaterias() {
        return materias;
    }
}
