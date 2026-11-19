import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TrabajosApiService } from './trabajos-api.service';

describe('TrabajosApiService', () => {
  let service: TrabajosApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TrabajosApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('usa una URL relativa para funcionar tras el proxy', () => {
    service.listar().subscribe((trabajos) => expect(trabajos).toEqual([]));

    const request = http.expectOne('/api/trabajos');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('envía los datos al crear un trabajo', () => {
    service.crear('REVISION', 'Pre-ITV').subscribe();

    const request = http.expectOne('/api/trabajos');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ tipo: 'REVISION', descripcion: 'Pre-ITV' });
    request.flush({});
  });
  it('envía la clave administrativa solo mediante cabecera en reset y seed', () => {
    const token = 'test-only-admin-token';
    service.resetDb(token).subscribe();
    const reset = http.expectOne('/api/db/reset');
    expect(reset.request.method).toBe('POST');
    expect(reset.request.headers.get('X-Admin-Token')).toBe(token);
    expect(reset.request.body).toEqual({});
    reset.flush('BBDD reseteada');

    service.seedDb(token).subscribe();
    const seed = http.expectOne('/api/db/seed');
    expect(seed.request.method).toBe('POST');
    expect(seed.request.headers.get('X-Admin-Token')).toBe(token);
    seed.flush('Datos de ejemplo cargados');
  });

});
