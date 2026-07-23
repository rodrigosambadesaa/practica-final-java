import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TipoMaterial, Trabajo, TrabajoTipo } from './trabajo.model';

@Injectable({ providedIn: 'root' })
export class TrabajosApiService {
    private readonly apiBase = '/api';

    constructor(private readonly http: HttpClient) { }

    listar(): Observable<Trabajo[]> {
        return this.http.get<Trabajo[]>(`${this.apiBase}/trabajos`);
    }

    crear(tipo: TrabajoTipo, descripcion: string): Observable<Trabajo> {
        return this.http.post<Trabajo>(`${this.apiBase}/trabajos`, { tipo, descripcion });
    }

    aumentarHoras(id: number, horas: number): Observable<Trabajo> {
        return this.http.patch<Trabajo>(`${this.apiBase}/trabajos/${id}/horas`, { horas });
    }

    aumentarMaterial(id: number, tipoMaterial: TipoMaterial, coste: number): Observable<Trabajo> {
        return this.http.patch<Trabajo>(`${this.apiBase}/trabajos/${id}/material`, { tipoMaterial, coste });
    }

    finalizar(id: number): Observable<Trabajo> {
        return this.http.patch<Trabajo>(`${this.apiBase}/trabajos/${id}/finalizar`, {});
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiBase}/trabajos/${id}`);
    }

    resetDb(): Observable<string> {
        return this.http.post(`${this.apiBase}/db/reset`, {}, { responseType: 'text' });
    }

    seedDb(): Observable<string> {
        return this.http.post(`${this.apiBase}/db/seed`, {}, { responseType: 'text' });
    }
}
