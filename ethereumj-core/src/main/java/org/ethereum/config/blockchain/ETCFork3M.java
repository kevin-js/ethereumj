package org.ethereum.config.blockchain;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

import static org.ethereum.util.BIUtil.max;

/**
 * Ethereum Classic HF on Block #3_000_000:
 * - EXP reprice (EIP-160)
 * - Replay Protection (EIP-155) (chainID: 61)
 * - Difficulty Bomb delay (ECIP-1010) (https://github.com/ethereumproject/ECIPs/blob/master/ECIPs/ECIP-1010.md)
 *
 * Created by Anton Nashatyrev on 13.01.2017.
 */
public class ETCFork3M extends Eip160HFConfig {
    public ETCFork3M(BlockchainConfig parent) {
        super(parent);
    }

    @Override
    public Integer getChainId() {
        return 61;
    }

    @Override
    public boolean eip161() {
        return false;
    }

    @Override
    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
        BigInteger pd = parent.getDifficultyBI();
        BigInteger quotient = pd.divide(getConstants().getDIFFICULTY_BOUND_DIVISOR());

        BigInteger sign = getCalcDifficultyMultiplier(curBlock, parent);

        BigInteger fromParent = pd.add(quotient.multiply(sign));
        BigInteger difficulty = max(getConstants().getMINIMUM_DIFFICULTY(), fromParent);

        int explosion = getExplosion(curBlock, parent);

        if (explosion >= 0) {
            difficulty = max(getConstants().getMINIMUM_DIFFICULTY(), difficulty.add(BigInteger.ONE.shiftLeft(explosion)));
        }

        return difficulty;
    }

    protected BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return BigInteger.valueOf(Math.max(1 - (curBlock.getTimestamp() - parent.getTimestamp()) / 10, -99));
    }


    protected int getExplosion(BlockHeader curBlock, BlockHeader parent) {
        int pauseBlock = 3000000;
        int contBlock = 5000000;
        int delay = (contBlock - pauseBlock) / 100000;
        int fixedDiff = (pauseBlock / 100000) - 2;

        if (curBlock.getNumber() < contBlock) {
            return fixedDiff;
        } else {
            return (int) ((curBlock.getNumber() / 100000) - delay - 2);
        }
    }
}
