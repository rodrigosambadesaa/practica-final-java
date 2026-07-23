import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { TipoMaterial, Trabajo, TrabajoTipo } from './api/trabajo.model';
import { TrabajosApiService } from './api/trabajos-api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CurrencyPipe, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  readonly trabajos = signal<Trabajo[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly mensaje = signal('');

  tipo: TrabajoTipo = 'REPARACION_MECANICA';
  descripcion = '';
  horas = 1;
  coste = 10;
  tipoMaterial: TipoMaterial = 'piezas';
  trabajoSeleccionadoId: number | null = null;

  constructor(private readonly api: TrabajosApiService) {}

  ngOnInit(): void {
    this.recargar();
  }

  get trabajoSeleccionado(): Trabajo | undefined {
    return this.trabajos().find((trabajo) => trabajo.id === this.trabajoSeleccionadoId);
  }

  get puedeAnadirMaterial(): boolean {
    return !!this.trabajoSeleccionado && this.trabajoSeleccionado.tipo !== 'REVISION'
      && !this.trabajoSeleccionado.finalizado;
  }

  get trabajosFinalizados(): number {
    return this.trabajos().filter((trabajo) => trabajo.finalizado).length;
  }

  recargar(): void {
    this.error.set('');
    this.cargando.set(true);
    this.api
      .listar()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (data) => {
          this.trabajos.set(data);
          if (data.length === 0) {
            this.trabajoSeleccionadoId = null;
          } else if (!data.some((trabajo) => trabajo.id === this.trabajoSeleccionadoId)) {
            this.trabajoSeleccionadoId = data[0].id;
          }
          this.ajustarMaterialAlTrabajo();
        },
        error: (err) => this.error.set(this.extraerError(err))
      });
  }

  crearTrabajo(): void {
    this.mensaje.set('');
    this.error.set('');
    this.api.crear(this.tipo, this.descripcion).subscribe({
      next: () => {
        this.descripcion = '';
        this.mensaje.set('Trabajo creado');
        this.recargar();
      },
      error: (err) => this.error.set(this.extraerError(err))
    });
  }

  aumentarHoras(): void {
    if (this.trabajoSeleccionadoId === null) {
      this.error.set('Selecciona un trabajo');
      return;
    }

    this.api.aumentarHoras(this.trabajoSeleccionadoId, this.horas).subscribe({
      next: () => {
        this.mensaje.set('Horas actualizadas');
        this.recargar();
      },
      error: (err) => this.error.set(this.extraerError(err))
    });
  }

  aumentarMaterial(): void {
    if (this.trabajoSeleccionadoId === null) {
      this.error.set('Selecciona un trabajo');
      return;
    }

    this.api.aumentarMaterial(this.trabajoSeleccionadoId, this.tipoMaterial, this.coste).subscribe({
      next: () => {
        this.mensaje.set('Material actualizado');
        this.recargar();
      },
      error: (err) => this.error.set(this.extraerError(err))
    });
  }

  finalizarTrabajo(): void {
    if (this.trabajoSeleccionadoId === null) {
      this.error.set('Selecciona un trabajo');
      return;
    }

    this.api.finalizar(this.trabajoSeleccionadoId).subscribe({
      next: () => {
        this.mensaje.set('Trabajo finalizado');
        this.recargar();
      },
      error: (err) => this.error.set(this.extraerError(err))
    });
  }

  eliminarTrabajo(): void {
    if (this.trabajoSeleccionadoId === null) {
      this.error.set('Selecciona un trabajo');
      return;
    }

    this.api.eliminar(this.trabajoSeleccionadoId).subscribe({
      next: () => {
        this.mensaje.set('Trabajo eliminado');
        this.recargar();
      },
      error: (err) => this.error.set(this.extraerError(err))
    });
  }

  resetDb(): void {
    this.api.resetDb().subscribe({
      next: () => {
        this.mensaje.set('BBDD reseteada');
        this.recargar();
      },
      error: (err) => this.error.set(this.extraerError(err))
    });
  }

  seedDb(): void {
    this.api.seedDb().subscribe({
      next: () => {
        this.mensaje.set('Datos de ejemplo cargados');
        this.recargar();
      },
      error: (err) => this.error.set(this.extraerError(err))
    });
  }

  ajustarMaterialAlTrabajo(): void {
    if (this.trabajoSeleccionado?.tipo === 'REPARACION_MECANICA') {
      this.tipoMaterial = 'piezas';
    }
  }

  etiquetaTipo(tipo: TrabajoTipo): string {
    const etiquetas: Record<TrabajoTipo, string> = {
      REPARACION_MECANICA: 'Mecánica',
      REPARACION_CHAPA_PINTURA: 'Chapa y pintura',
      REVISION: 'Revisión'
    };
    return etiquetas[tipo];
  }

  private extraerError(err: unknown): string {
    if (!(err instanceof HttpErrorResponse)) {
      return 'Se ha producido un error inesperado';
    }
    if (typeof err.error?.detail === 'string') {
      return err.error.detail;
    }
    if (typeof err.error?.message === 'string') {
      return err.error.message;
    }
    if (typeof err?.error === 'string') {
      return err.error;
    }
    return 'No se ha podido comunicar con la API';
  }
}
