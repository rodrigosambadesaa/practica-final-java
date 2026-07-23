export type TrabajoTipo =
    | 'REPARACION_MECANICA'
    | 'REPARACION_CHAPA_PINTURA'
    | 'REVISION';

export type TipoMaterial = 'piezas' | 'pintura' | 'chapa';

export interface Trabajo {
    id: number;
    tipo: TrabajoTipo;
    descripcion: string;
    horas: number;
    costePiezas: number;
    costePintura: number;
    costeChapa: number;
    finalizado: boolean;
    eliminado: boolean;
    plazoDias: number;
    precio: number;
}
