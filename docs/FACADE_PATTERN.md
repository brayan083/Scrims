# Patrón Facade en Scrims Application

## Resumen

Este documento explica la implementación del patrón Facade en la aplicación Scrims, específicamente en el servicio `ScrimFacade`.

## ¿Qué es el Patrón Facade?

El patrón Facade es un patrón de diseño estructural que proporciona una interfaz simplificada a un conjunto complejo de subsistemas. En nuestra aplicación, el Facade coordina operaciones complejas que involucran múltiples servicios especializados.

## Arquitectura de Servicios

### Servicios Especializados (SRP - Single Responsibility Principle)

1. **PostulationService**: Gestión de postulaciones a scrims
   - Aplicar a un scrim
   - Aceptar postulaciones
   - Listar postulaciones

2. **TeamManagementService**: Gestión de equipos y lobby
   - Agregar jugadores al lobby
   - Ejecutar comandos de equipo

3. **ScrimFinalizationService**: Finalización y MMR
   - Confirmar participación
   - Finalizar scrim
   - Actualizar MMR de jugadores

4. **StatisticsService**: Gestión de estadísticas
   - Guardar estadísticas de jugadores
   - Obtener estadísticas de un scrim

5. **ScrimService**: Ciclo de vida y consultas de scrims
   - Crear scrim
   - Buscar scrims
   - Obtener detalles
   - Cancelar scrim

### ScrimFacade: Coordinador de Operaciones Complejas (Interface + Implementación)

El `ScrimFacade` es una **interface** con su implementación `ScrimFacadeImpl` que actúa como orquestador para operaciones que requieren coordinación entre múltiples servicios:

```java
// Interface
public interface ScrimFacade {
    ScrimResponse createScrimWithTeams(CreateScrimRequest request, String username);
    Postulation applyToScrimWithAutoAccept(Long scrimId, String username);
    void finalizeScrimComplete(Long scrimId, FinalizeScrimRequest request, String username);
    void cancelScrimWithCleanup(Long scrimId, String username);
}

// Implementación
@Service
public class ScrimFacadeImpl implements ScrimFacade {
    @Autowired
    private ScrimService scrimService;
    @Autowired
    private PostulationService postulationService;
    @Autowired
    private ScrimFinalizationService scrimFinalizationService;
    
    // Métodos que coordinan múltiples servicios...
}
```

**¿Por qué Interface + Implementación?**
- ✅ **Testabilidad**: Fácil crear mocks del Facade para pruebas
- ✅ **Inversión de dependencias**: Controller depende de abstracción, no de implementación concreta
- ✅ **Flexibilidad**: Permite múltiples implementaciones (ej: producción, desarrollo, testing)
- ✅ **Spring-friendly**: Facilita el uso de proxies y AOP

## Cuándo Usar ScrimFacade vs Servicios Directos

### Usa ScrimFacade cuando:

✅ **Operación compleja que coordina múltiples servicios**
- Crear scrim con equipos iniciales
- Aplicar a scrim con auto-aceptación
- Finalizar scrim con estadísticas y MMR
- Cancelar scrim con limpieza completa

**Ejemplo en Controller:**
```java
@PostMapping
public ResponseEntity<ScrimResponse> createScrim(@RequestBody CreateScrimRequest request, Principal principal) {
    // Operación compleja: coordina scrim creation + team setup
    ScrimResponse createdScrim = scrimFacade.createScrimWithTeams(request, principal.getName());
    return ResponseEntity.status(201).body(createdScrim);
}
```

### Usa Servicios Directos cuando:

✅ **Operación simple de un solo servicio**
- Buscar scrims (solo ScrimService)
- Obtener detalles (solo ScrimService)
- Listar postulaciones (solo PostulationService)
- Obtener estadísticas (solo StatisticsService)

**Ejemplo en Controller:**
```java
@GetMapping("/{scrimId}/statistics")
public ResponseEntity<List<StatisticResponseDTO>> getScrimStatistics(@PathVariable Long scrimId) {
    // Operación simple: solo consulta a StatisticsService
    List<StatisticResponseDTO> stats = statisticsService.getScrimStatistics(scrimId);
    return ResponseEntity.ok(stats);
}
```

## Ventajas de Esta Arquitectura

### 1. Single Responsibility Principle (SRP)
Cada servicio tiene una responsabilidad única y bien definida:
- PostulationService → Postulaciones
- TeamManagementService → Equipos/Lobby
- ScrimFinalizationService → Finalización/MMR
- StatisticsService → Estadísticas
- ScrimService → Ciclo de vida de scrims

### 2. Separation of Concerns
- Controller → Manejo de HTTP y validación de entrada
- Facade → Coordinación de operaciones complejas
- Services → Lógica de negocio especializada
- Mapper → Transformación de DTOs
- Repository → Acceso a datos

### 3. Testabilidad
Cada servicio puede ser testeado de forma independiente con mocks de sus dependencias.

### 4. Mantenibilidad
Cambios en un servicio no afectan a otros servicios. El Facade aísla la complejidad de coordinación.

### 5. Extensibilidad
Fácil agregar nuevos servicios o modificar la orquestación en el Facade sin tocar los servicios individuales.

## Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                   ScrimController                       │
│  (REST Endpoints - HTTP Layer)                         │
└────────────┬────────────────────────────────┬───────────┘
             │                                │
             │ Operaciones Complejas          │ Operaciones Simples
             │                                │
             ▼                                ▼
    ┌─────────────────┐            ┌──────────────────────┐
    │  ScrimFacade    │            │ Servicios Directos:  │
    │  (Interface)    │            │ - ScrimService       │
    │       ↓         │            │ - PostulationService │
    │ ScrimFacadeImpl │            │ - TeamManagement...  │
    │                 │            │ - ScrimFinalization..│
    │ Coordina:       │            │ - StatisticsService  │
    │ ├─ ScrimService │            └──────────────────────┘
    │ ├─ Postulation  │
    │ ├─ TeamMgmt     │
    │ └─ Finalization │
    └─────────────────┘
```

## Ejemplos de Uso en Controller

### Operación Compleja con Facade

```java
@PostMapping("/{scrimId}/finalize")
public ResponseEntity<String> finalizeScrim(
        @PathVariable Long scrimId,
        @RequestBody FinalizeScrimRequest request,
        Principal principal) {
    // Facade coordina: finalización + guardar estadísticas + actualizar MMR
    scrimFacade.finalizeScrimComplete(scrimId, request, principal.getName());
    return ResponseEntity.ok("Scrim finalizado y estadísticas cargadas correctamente.");
}
```

### Operación Simple con Servicio Directo

```java
@PostMapping("/{scrimId}/postulations/{postulationId}/accept")
public ResponseEntity<Postulation> acceptPostulation(
        @PathVariable Long scrimId,
        @PathVariable Long postulationId,
        Principal principal) {
    // Operación simple: solo PostulationService
    Postulation postulation = postulationService.acceptPostulation(
        scrimId, postulationId, principal.getName()
    );
    return ResponseEntity.ok(postulation);
}
```

## Guía de Decisión Rápida

| Pregunta | Respuesta | Usar |
|----------|-----------|------|
| ¿La operación requiere llamar a 2+ servicios? | Sí | **ScrimFacade** |
| ¿La operación tiene lógica de coordinación compleja? | Sí | **ScrimFacade** |
| ¿Es solo una consulta simple? | Sí | **Servicio Directo** |
| ¿Solo modifica una entidad? | Sí | **Servicio Directo** |
| ¿Necesita orquestar múltiples pasos? | Sí | **ScrimFacade** |

## Futuras Extensiones

El Facade puede extenderse fácilmente para incluir:
- Sistema de notificaciones (notificar a jugadores sobre cambios)
- Sistema de reembolsos (procesar cancelaciones con devolución)
- Sistema de auditoría (registrar todas las operaciones importantes)
- Sistema de eventos (publicar eventos de dominio)

Estas extensiones se agregan al Facade sin modificar los servicios especializados.

## Referencias

- [Gang of Four - Facade Pattern](https://refactoring.guru/design-patterns/facade)
- [SOLID Principles - Single Responsibility](https://en.wikipedia.org/wiki/Single-responsibility_principle)
- [Spring Framework Best Practices](https://spring.io/guides)

---

**Última actualización**: Diciembre 2024  
**Autor**: Scrims Development Team
