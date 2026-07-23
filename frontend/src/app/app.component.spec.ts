import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Trabajo } from './api/trabajo.model';
import { TrabajosApiService } from './api/trabajos-api.service';
import { AppComponent } from './app.component';

registerLocaleData(localeEs);

describe('AppComponent', () => {
  const trabajo: Trabajo = {
    id: 7,
    tipo: 'REVISION',
    descripcion: 'Revisión anual',
    horas: 1.5,
    costePiezas: 0,
    costePintura: 0,
    costeChapa: 0,
    finalizado: false,
    eliminado: false,
    plazoDias: 7,
    precio: 65
  };

  const api = {
    listar: vi.fn(() => of([trabajo])),
    crear: vi.fn(() => of(trabajo)),
    aumentarHoras: vi.fn(() => of(trabajo)),
    aumentarMaterial: vi.fn(() => of(trabajo)),
    finalizar: vi.fn(() => of({ ...trabajo, finalizado: true })),
    eliminar: vi.fn(() => of(void 0)),
    resetDb: vi.fn(() => of('ok')),
    seedDb: vi.fn(() => of('ok'))
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        { provide: TrabajosApiService, useValue: api },
        { provide: LOCALE_ID, useValue: 'es-ES' }
      ]
    }).compileComponents();
  });

  it('crea el componente y carga los trabajos', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(fixture.componentInstance.trabajos()).toEqual([trabajo]);
    expect(api.listar).toHaveBeenCalledOnce();
  });

  it('renderiza el panel y el trabajo recibido', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Trabajos bajo control');
    expect(compiled.textContent).toContain('Revisión anual');
    expect(compiled.textContent).toContain('65,00');
  });

  it('crea un trabajo válido y limpia la descripción', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const component = fixture.componentInstance;
    component.descripcion = 'Revisión anual';
    component.tipo = 'REVISION';

    component.crearTrabajo();

    expect(api.crear).toHaveBeenCalledWith('REVISION', 'Revisión anual');
    expect(component.descripcion).toBe('');
    expect(component.mensaje()).toBe('Trabajo creado');
  });
});
