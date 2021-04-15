package Aula;

public class Aula {

    //principal
    public static void main(String[] args) {

        Alumno alum = new Alumno();
        alum.setNombre("Ro");
        alum.setApellido("TukiTuki");
        alum.setEdad(15);
        alum.setCantMat(3);

        System.out.println(alum.getNombre());
        System.out.println(alum.getApellido());
        System.out.println(alum.getEdad());
        System.out.println(alum.getCantMat());

    }
}
