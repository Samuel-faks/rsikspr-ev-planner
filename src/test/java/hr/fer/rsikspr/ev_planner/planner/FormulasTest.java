package hr.fer.rsikspr.ev_planner.planner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Formulas F1-F8, checked against the worked example in table 7.2 (C=60, k=18, Pv=150, d=120, Ps=50, T=5). */
class FormulasTest {

    private static final double EPS = 1e-9;

    @ParameterizedTest
    @CsvSource({"30, 1.00", "15, 1.00", "14.9, 1.15", "5, 1.15", "0, 1.15", "-0.1, 1.30", "-5, 1.30"})
    void f1TemperatureFactor(double temperature, double expected) {
        assertThat(Formulas.temperatureFactor(temperature)).isEqualTo(expected);
    }

    @Test
    void f2Energy() {
        assertThat(Formulas.energyKwh(120, 18, 1.15)).isCloseTo(24.84, within(EPS));
    }

    @Test
    void f3DrivingTime() {
        assertThat(Formulas.drivingMinutes(120)).isCloseTo(80, within(EPS));
    }

    @Test
    void f5ReachableFromAStopAt80Percent() {
        assertThat(Formulas.isReachable(24.84, 80, 10, 60)).isTrue();   // 24.84 <= 42
    }

    @Test
    void f5NotReachableFromStartWith40Percent() {
        assertThat(Formulas.isReachable(24.84, 40, 10, 60)).isFalse();  // 24.84 > 18
    }

    @Test
    void f5BoundaryIsInclusive() {
        // table 7.3 example 3: A -> D needs exactly 20 kWh, (50 - 10) / 100 * 50 = 20 is available
        assertThat(Formulas.isReachable(20, 50, 10, 50)).isTrue();
        // 0.3 - 0.1 would give 0.19999999999999998; the percent difference must be taken first
        assertThat(Formulas.isReachable(10, 30, 10, 50)).isTrue();
    }

    @Test
    void f6ArrivalSoc() {
        assertThat(Formulas.arrivalSocPercent(80, 24.84, 60)).isCloseTo(38.6, within(EPS));
    }

    @Test
    void f7ChargeEnergyAndTime() {
        double charge = Formulas.chargeKwh(80, 24.84, 60);
        assertThat(charge).isCloseTo(24.84, within(EPS));   // from 80 % you top up what you used
        assertThat(Formulas.chargingMinutes(charge, 150, 50)).isCloseTo(29.808, within(EPS));
    }

    @Test
    void f7NoChargingWhenArrivingAbove80Percent() {
        // start with 100 %, use 6 kWh of 60 -> arrive with 90 %
        assertThat(Formulas.chargeKwh(100, 6, 60)).isZero();
        assertThat(Formulas.chargingMinutes(0, 150, 50)).isZero();
    }

    @Test
    void f8EdgeWeight() {
        assertThat(Formulas.edgeWeight(80, 29.808, false)).isCloseTo(119.808, within(EPS));
        assertThat(Formulas.edgeWeight(80, 29.808, true)).isCloseTo(80, within(EPS));
    }
}
