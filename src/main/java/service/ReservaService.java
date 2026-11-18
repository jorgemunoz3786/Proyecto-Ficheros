package service;

import model.Reserva;

import java.util.ArrayList;

public class ReservaService {

    /** Dos reservas son del mismo viaje si coinciden nombre, origen y destino. */
    static boolean mismoViaje(Reserva a, Reserva b) {
        return a.getNombre().equalsIgnoreCase(b.getNombre())
                && a.getOrigen().equals(b.getOrigen())
                && a.getDestino().equals(b.getDestino());
    }

    /**
     * Si 2 o más reservas coinciden en nombre, origen y destino, todas reciben
     * el mismo localizador (LOC-AIR-001, LOC-AIR-002...).
     */
    public static void asignarLocalizadores(ArrayList<Reserva> reservas) {
        int contador = 1;

        for (int i = 0; i < reservas.size(); i++) {
            Reserva actual = reservas.get(i);
            if (!actual.getLocalizador().isEmpty()) continue;

            int coincidencias = 0;
            for (int j = 0; j < reservas.size(); j++) {
                if (mismoViaje(actual, reservas.get(j))) coincidencias++;
            }

            if (coincidencias >= 2) {
                String localizador = String.format("LOC-AIR-%03d", contador);
                contador++;
                for (int j = 0; j < reservas.size(); j++) {
                    if (mismoViaje(actual, reservas.get(j))) {
                        reservas.get(j).setLocalizador(localizador);
                    }
                }
            }
        }
    }
}