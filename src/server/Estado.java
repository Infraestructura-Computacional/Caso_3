package server;

public enum Estado {
     ESTADO_0(0, "ENOFICINA"),
     ESTADO_1(1, "RECOGIDO"),
     ESTADO_2(2, "ENCLASIFICACION"),
     ESTADO_3(3, "DESPACHADO"),
     ESTADO_4(4, "ENENTREGA"),
     ESTADO_5(5, "ENTREGADO"),
     ESTADO_6(6, "DESCONOCIDO");
 
     private final int codigo;
     private final String descripcion;
 
     Estado(int codigo, String descripcion) {
         this.codigo = codigo;
         this.descripcion = descripcion;
     }
 
     public static String obtenerDescripcion(int codigo) {
         for (Estado estado : Estado.values()) {
             if (estado.codigo == codigo) {
                 return estado.descripcion;
             }
         }
         return "Desconocido";
     }
 }
 