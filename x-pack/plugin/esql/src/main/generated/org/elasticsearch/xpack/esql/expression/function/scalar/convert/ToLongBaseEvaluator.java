// Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
// or more contributor license agreements. Licensed under the Elastic License
// 2.0; you may not use this file except in compliance with the Elastic License
// 2.0.
package org.elasticsearch.xpack.esql.expression.function.scalar.convert;

import java.lang.IllegalArgumentException;
import java.lang.Override;
import java.lang.String;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.RamUsageEstimator;
import org.elasticsearch.compute.data.Block;
import org.elasticsearch.compute.data.BytesRefBlock;
import org.elasticsearch.compute.data.BytesRefVector;
import org.elasticsearch.compute.data.IntBlock;
import org.elasticsearch.compute.data.IntVector;
import org.elasticsearch.compute.data.LongBlock;
import org.elasticsearch.compute.data.LongVector;
import org.elasticsearch.compute.data.Page;
import org.elasticsearch.compute.operator.DriverContext;
import org.elasticsearch.compute.operator.EvalOperator;
import org.elasticsearch.compute.operator.Warnings;
import org.elasticsearch.core.Releasables;
import org.elasticsearch.xpack.esql.core.tree.Source;

/**
 * {@link EvalOperator.ExpressionEvaluator} implementation for {@link ToLongBase}.
 * This class is generated. Edit {@code EvaluatorImplementer} instead.
 */
public final class ToLongBaseEvaluator implements EvalOperator.ExpressionEvaluator {
  private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(ToLongBaseEvaluator.class);

  private final Source source;

  private final EvalOperator.ExpressionEvaluator in;

  private final EvalOperator.ExpressionEvaluator radix;

  private final DriverContext driverContext;

  private Warnings warnings;

  public ToLongBaseEvaluator(Source source, EvalOperator.ExpressionEvaluator in,
      EvalOperator.ExpressionEvaluator radix, DriverContext driverContext) {
    this.source = source;
    this.in = in;
    this.radix = radix;
    this.driverContext = driverContext;
  }

  @Override
  public Block eval(Page page) {
    try (BytesRefBlock inBlock = (BytesRefBlock) in.eval(page)) {
      try (IntBlock radixBlock = (IntBlock) radix.eval(page)) {
        BytesRefVector inVector = inBlock.asVector();
        if (inVector == null) {
          return eval(page.getPositionCount(), inBlock, radixBlock);
        }
        IntVector radixVector = radixBlock.asVector();
        if (radixVector == null) {
          return eval(page.getPositionCount(), inBlock, radixBlock);
        }
        return eval(page.getPositionCount(), inVector, radixVector).asBlock();
      }
    }
  }

  @Override
  public long baseRamBytesUsed() {
    long baseRamBytesUsed = BASE_RAM_BYTES_USED;
    baseRamBytesUsed += in.baseRamBytesUsed();
    baseRamBytesUsed += radix.baseRamBytesUsed();
    return baseRamBytesUsed;
  }

  public LongBlock eval(int positionCount, BytesRefBlock inBlock, IntBlock radixBlock) {
    try(LongBlock.Builder result = driverContext.blockFactory().newLongBlockBuilder(positionCount)) {
      BytesRef inScratch = new BytesRef();
      position: for (int p = 0; p < positionCount; p++) {
        if (inBlock.isNull(p)) {
          result.appendNull();
          continue position;
        }
        if (inBlock.getValueCount(p) != 1) {
          if (inBlock.getValueCount(p) > 1) {
            warnings().registerException(new IllegalArgumentException("single-value function encountered multi-value"));
          }
          result.appendNull();
          continue position;
        }
        if (radixBlock.isNull(p)) {
          result.appendNull();
          continue position;
        }
        if (radixBlock.getValueCount(p) != 1) {
          if (radixBlock.getValueCount(p) > 1) {
            warnings().registerException(new IllegalArgumentException("single-value function encountered multi-value"));
          }
          result.appendNull();
          continue position;
        }
        BytesRef in = inBlock.getBytesRef(inBlock.getFirstValueIndex(p), inScratch);
        int radix = radixBlock.getInt(radixBlock.getFirstValueIndex(p));
        result.appendLong(ToLongBase.process(in, radix));
      }
      return result.build();
    }
  }

  public LongVector eval(int positionCount, BytesRefVector inVector, IntVector radixVector) {
    try(LongVector.FixedBuilder result = driverContext.blockFactory().newLongVectorFixedBuilder(positionCount)) {
      BytesRef inScratch = new BytesRef();
      position: for (int p = 0; p < positionCount; p++) {
        BytesRef in = inVector.getBytesRef(p, inScratch);
        int radix = radixVector.getInt(p);
        result.appendLong(p, ToLongBase.process(in, radix));
      }
      return result.build();
    }
  }

  @Override
  public String toString() {
    return "ToLongBaseEvaluator[" + "in=" + in + ", radix=" + radix + "]";
  }

  @Override
  public void close() {
    Releasables.closeExpectNoException(in, radix);
  }

  private Warnings warnings() {
    if (warnings == null) {
      this.warnings = Warnings.createWarnings(
              driverContext.warningsMode(),
              source.source().getLineNumber(),
              source.source().getColumnNumber(),
              source.text()
          );
    }
    return warnings;
  }

  static class Factory implements EvalOperator.ExpressionEvaluator.Factory {
    private final Source source;

    private final EvalOperator.ExpressionEvaluator.Factory in;

    private final EvalOperator.ExpressionEvaluator.Factory radix;

    public Factory(Source source, EvalOperator.ExpressionEvaluator.Factory in,
        EvalOperator.ExpressionEvaluator.Factory radix) {
      this.source = source;
      this.in = in;
      this.radix = radix;
    }

    @Override
    public ToLongBaseEvaluator get(DriverContext context) {
      return new ToLongBaseEvaluator(source, in.get(context), radix.get(context), context);
    }

    @Override
    public String toString() {
      return "ToLongBaseEvaluator[" + "in=" + in + ", radix=" + radix + "]";
    }
  }
}
