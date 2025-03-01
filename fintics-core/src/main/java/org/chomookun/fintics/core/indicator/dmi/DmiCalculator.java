package org.chomookun.fintics.core.indicator.dmi;

import org.chomookun.fintics.core.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DmiCalculator extends IndicatorCalculator<DmiContext, Dmi> {

    /**
     * Constructor
     * @param context
     */
    public DmiCalculator(DmiContext context) {
        super(context);
    }

    /**
     * Calculates dmi
     * @param series series
     * @return series of dmi
     */
    @Override
    public List<Dmi> calculate(List<Ohlcv> series) {
        List<BigDecimal> highSeries = series.stream()
                .map(Ohlcv::getHigh)
                .toList();
        List<BigDecimal> lowSeries = series.stream()
                .map(Ohlcv::getLow)
                .toList();
        List<BigDecimal> closeSeries = series.stream()
                .map(Ohlcv::getClose)
                .toList();
        List<BigDecimal> pdms = new ArrayList<>();
        List<BigDecimal> mdms = new ArrayList<>();
        List<BigDecimal> trs = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++ ) {
            BigDecimal high = highSeries.get(i);
            BigDecimal low = lowSeries.get(i);
            BigDecimal previousHigh = highSeries.get(Math.max(i-1, 0));
            BigDecimal previousLow = lowSeries.get(Math.max(i-1, 0));
            BigDecimal previousClose = closeSeries.get(Math.max(i-1, 0));
            // pdm, mdm, tr
            BigDecimal pdm = calculatePdm(high, low, previousHigh, previousLow);
            BigDecimal mdm = calculateMdm(high, low, previousHigh, previousLow);
            BigDecimal tr = calculateTr(high, low, previousClose);
            // adds
            pdms.add(pdm);
            mdms.add(mdm);
            trs.add(tr);
        }
        // average
        pdms = emas(pdms, getContext().getPeriod(), getContext().getMathContext());
        mdms = emas(mdms, getContext().getPeriod(), getContext().getMathContext());
        trs = emas(trs, getContext().getPeriod(), getContext().getMathContext());
        List<BigDecimal> pdis = new ArrayList<>();
        List<BigDecimal> mdis = new ArrayList<>();
        List<BigDecimal> dxs = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++) {
            BigDecimal pdi = calculatePdi(pdms.get(i), trs.get(i));
            BigDecimal mdi = calculateMdi(mdms.get(i), trs.get(i));
            pdis.add(pdi);
            mdis.add(mdi);
            // dx
            BigDecimal dx = calculateDx(pdi, mdi);
            dxs.add(dx);
        }
        // average
        List<BigDecimal> adxs = emas(dxs, getContext().getPeriod(), getContext().getMathContext());
        // dmi
        List<Dmi> dmis = new ArrayList<>();
        for(int i = 0, size = series.size(); i < size; i ++) {
            Dmi dmi = Dmi.builder()
                    .dateTime(series.get(i).getDateTime())
                    .pdi(pdis.get(i))
                    .mdi(mdis.get(i))
                    .adx(adxs.get(i))
                    .build();
            dmis.add(dmi);
        }
        return dmis;
    }

    /**
     * Calculates pdm
     * @param high high
     * @param low low
     * @param previousHigh previous high
     * @param previousLow previous low
     * @return pdm
     */
    private BigDecimal calculatePdm(BigDecimal high, BigDecimal low, BigDecimal previousHigh, BigDecimal previousLow) {
        BigDecimal upMove = high.subtract(previousHigh);
        BigDecimal downMove = previousLow.subtract(low);
        if(upMove.compareTo(downMove) > 0 && upMove.compareTo(BigDecimal.ZERO) > 0) {
            return upMove;
        }else{
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calculates mdm
     * @param high high
     * @param low low
     * @param previousHigh previous high
     * @param previousLow previous low
     * @return mdm
     */
    private BigDecimal calculateMdm(BigDecimal high, BigDecimal low, BigDecimal previousHigh, BigDecimal previousLow) {
        BigDecimal upMove = high.subtract(previousHigh);
        BigDecimal downMove = previousLow.subtract(low);
        if(downMove.compareTo(upMove) > 0 && downMove.compareTo(BigDecimal.ZERO) > 0) {
            return downMove;
        }else{
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calculates tr
     * @param high high
     * @param low low
     * @param previousClose previous close
     * @return tr
     */
    private BigDecimal calculateTr(BigDecimal high, BigDecimal low, BigDecimal previousClose) {
        BigDecimal hl = high.subtract(low);
        BigDecimal hc = high.subtract(previousClose);
        BigDecimal cl = previousClose.subtract(low);
        return hl.abs().max(hc.abs()).max(cl.abs());
    }

    /**
     * Calculates pdi
     * @param pdm pdm
     * @param tr tr
     * @return pdi
     */
    private BigDecimal calculatePdi(BigDecimal pdm, BigDecimal tr) {
        if(tr.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return pdm.divide(tr, getContext().getMathContext())
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculates mdi
     * @param mdm mdm
     * @param tr tr
     * @return mdi
     */
    public BigDecimal calculateMdi(BigDecimal mdm, BigDecimal tr) {
        if(tr.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return mdm.divide(tr, getContext().getMathContext())
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculates dx
     * @param pdi pdi
     * @param mdi mdi
     * @return dx
     */
    private BigDecimal calculateDx(BigDecimal pdi, BigDecimal mdi) {
        if(pdi.add(mdi).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return pdi.subtract(mdi).abs()
                .divide(pdi.add(mdi), getContext().getMathContext())
                .multiply(BigDecimal.valueOf(100));
    }

}
