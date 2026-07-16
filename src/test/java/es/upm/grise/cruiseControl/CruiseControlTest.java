package es.upm.grise.cruiseControl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import es.upm.grise.cruiseControl.exceptions.CannotSetSpeedLimitException;
import es.upm.grise.cruiseControl.exceptions.IncorrectSpeedLimitException;
import es.upm.grise.cruiseControl.exceptions.IncorrectSpeedSetException;
import es.upm.grise.cruiseControl.exceptions.SpeedSetAboveSpeedLimitException;

class CruiseControlTest {

	// Valores por defecto
	private static final int VIA_MAX = 120;
	private static final int VIA_MIN = 60;

	private CruiseControl nuevoCruiseControl(int velocidadActual, int viaMax, int viaMin) {
		return new CruiseControl(new RoadInformation(viaMax, viaMin), new Speedometer(velocidadActual));
	}

	private CruiseControl nuevoCruiseControl(int velocidadActual) {
		return nuevoCruiseControl(velocidadActual, VIA_MAX, VIA_MIN);
	}

	private Command comandoPara(int velocidadActual, int viaMax, int viaMin, int speedSet) throws Exception {
		CruiseControl cc = nuevoCruiseControl(velocidadActual, viaMax, viaMin);
		cc.setSpeedSet(speedSet);
		return cc.nextCommand().command;
	}

	@Nested
	class EstadoInicial {

		@Test
		@DisplayName("Recien creado no esta activado")
		void recienCreadoNoEstaActivado() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertFalse(cc.isEnabled());
		}

		@Test
		@DisplayName("speedSet inicial es null")
		void speedSetInicialEsNull() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertNull(cc.getSpeedSet());
		}

		@Test
		@DisplayName("speedLimit inicial es null")
		void speedLimitInicialEsNull() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertNull(cc.getSpeedLimit());
		}

		@Test
		@DisplayName("Sin speedSet, nextCommand devuelve IDLE")
		void sinSpeedSetDevuelveIdle() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertEquals(Command.IDLE, cc.nextCommand().command);
		}
	}

	@Nested
	class SetSpeedSet {

		@Test
		@DisplayName("speedSet = 0 lanza IncorrectSpeedSetException")
		void ceroLanzaExcepcion() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertThrows(IncorrectSpeedSetException.class, () -> cc.setSpeedSet(0));
		}

		@Test
		@DisplayName("speedSet negativa lanza IncorrectSpeedSetException")
		void negativaLanzaExcepcion() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertThrows(IncorrectSpeedSetException.class, () -> cc.setSpeedSet(-10));
		}

		@Test
		@DisplayName("speedSet = 1 (minimo positivo) es aceptada")
		void minimoPositivoAceptado() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedSet(1);
			assertEquals(1, cc.getSpeedSet());
			assertTrue(cc.isEnabled());
		}

		@Test
		@DisplayName("Al fijar speedSet, el control pasa a estar activado")
		void fijarSpeedSetActivaElControl() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedSet(100);
			assertEquals(100, cc.getSpeedSet());
			assertTrue(cc.isEnabled());
		}

		@Test
		@DisplayName("Un speedSet invalido no activa el control ni guarda valor")
		void speedSetInvalidoNoActiva() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertThrows(IncorrectSpeedSetException.class, () -> cc.setSpeedSet(0));
			assertFalse(cc.isEnabled());
			assertNull(cc.getSpeedSet());
		}

		@Test
		@DisplayName("speedSet por debajo de speedLimit es aceptada")
		void speedSetBajoLimiteAceptada() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedLimit(120);
			cc.setSpeedSet(100);
			assertEquals(100, cc.getSpeedSet());
		}

		@Test
		void speedSetIgualLimiteAceptada() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedLimit(120);
			cc.setSpeedSet(120);
			assertEquals(120, cc.getSpeedSet());
		}

		@Test
		void speedSetSobreLimiteLanzaExcepcion() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedLimit(120);
			assertThrows(SpeedSetAboveSpeedLimitException.class, () -> cc.setSpeedSet(121));
		}
	}

	@Nested
	class SetSpeedLimit {

		@Test
		void ceroLanzaExcepcion() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertThrows(IncorrectSpeedLimitException.class, () -> cc.setSpeedLimit(0));
		}

		@Test
		void negativaLanzaExcepcion() {
			CruiseControl cc = nuevoCruiseControl(100);
			assertThrows(IncorrectSpeedLimitException.class, () -> cc.setSpeedLimit(-5));
		}

		@Test
		void positivaAceptada() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedLimit(120);
			assertEquals(120, cc.getSpeedLimit());
		}

		@Test
		void fijarLimiteNoActiva() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedLimit(120);
			assertFalse(cc.isEnabled());
			assertEquals(Command.IDLE, cc.nextCommand().command);
		}

		@Test
		void noSePuedeFijarLimiteTrasSpeedSet() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedSet(100);
			assertThrows(CannotSetSpeedLimitException.class, () -> cc.setSpeedLimit(120));
		}

		@Test
		void valorInvalidoTienePrioridadSobreEstado() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedSet(100);
			assertThrows(IncorrectSpeedLimitException.class, () -> cc.setSpeedLimit(0));
		}
	}

	@Nested
	class Disable {

		@Test
		void desactivaYReseteaSpeedSet() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedSet(100);
			cc.disable();
			assertFalse(cc.isEnabled());
			assertNull(cc.getSpeedSet());
		}

		@Test
		void trasDisableDevuelveIdle() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedSet(100);
			assertEquals(Command.KEEP, cc.nextCommand().command); // estaba controlado
			cc.disable();
			assertEquals(Command.IDLE, cc.nextCommand().command);
		}

		@Test
		void noBorraSpeedLimit() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedLimit(120);
			cc.setSpeedSet(100);
			cc.disable();
			assertEquals(120, cc.getSpeedLimit());
			assertNull(cc.getSpeedSet());
			assertFalse(cc.isEnabled());
		}

		@Test
		void disableSinControlEsSeguro() {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.disable();
			assertFalse(cc.isEnabled());
			assertNull(cc.getSpeedSet());
			assertEquals(Command.IDLE, cc.nextCommand().command);
		}
	}

	@Nested
	class NextCommand {

		@Test
		void igualASpeedSetKeep() throws Exception {
			assertEquals(Command.KEEP, comandoPara(100, VIA_MAX, VIA_MIN, 100));
		}

		@Test
		void superaSpeedSetReduce() throws Exception {
			assertEquals(Command.REDUCE, comandoPara(110, VIA_MAX, VIA_MIN, 100));
		}

		@Test
		void inferiorASpeedSetIncrease() throws Exception {
			assertEquals(Command.INCREASE, comandoPara(80, VIA_MAX, VIA_MIN, 100));
		}

		@Test
		void pordebajoMinimoIncrementaAunqueSupereSpeedSet() throws Exception {
			assertEquals(Command.INCREASE, comandoPara(50, VIA_MAX, VIA_MIN, 40));
		}

		@Test
		void porEncimaMaximoReduceAunqueInferiorASpeedSet() throws Exception {
			assertEquals(Command.REDUCE, comandoPara(130, VIA_MAX, VIA_MIN, 150));
		}

		@Test
		void enElMinimoSigueSpeedSet() throws Exception {
			assertEquals(Command.INCREASE, comandoPara(60, VIA_MAX, VIA_MIN, 100));
		}

		@Test
		void enElMaximoSigueSpeedSet() throws Exception {
			assertEquals(Command.REDUCE, comandoPara(120, VIA_MAX, VIA_MIN, 100));
		}

		@Test
		void justoPorDebajoDelMinimo() throws Exception {
			assertEquals(Command.INCREASE, comandoPara(59, VIA_MAX, VIA_MIN, 40));
		}

		@Test
		void justoPorEncimaDelMaximo() throws Exception {
			assertEquals(Command.REDUCE, comandoPara(121, VIA_MAX, VIA_MIN, 150));
		}

		@Test
		void bordeIgualSpeedSetBajoMinimo() throws Exception {
			assertEquals(Command.KEEP, comandoPara(50, VIA_MAX, VIA_MIN, 50));
		}

		@Test
		void bordeIgualSpeedSetSobreMaximo() throws Exception {
			assertEquals(Command.KEEP, comandoPara(130, VIA_MAX, VIA_MIN, 130));
		}
	}

	@Nested
	class Integrado {

		@Test
		void limiteLuegoSpeedSetKeep() throws Exception {
			CruiseControl cc = nuevoCruiseControl(100);
			cc.setSpeedLimit(120);
			cc.setSpeedSet(100);
			assertEquals(120, cc.getSpeedLimit());
			assertEquals(100, cc.getSpeedSet());
			assertTrue(cc.isEnabled());
			assertEquals(Command.KEEP, cc.nextCommand().command);
		}
	}

}
