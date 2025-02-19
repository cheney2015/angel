package com.tencent.angel.graph.client.initNeighbor4;

import com.tencent.angel.PartitionKey;
import com.tencent.angel.exception.AngelException;
import com.tencent.angel.graph.utils.LongIndexComparator;
import com.tencent.angel.ml.matrix.psf.update.base.PartitionUpdateParam;
import com.tencent.angel.ml.matrix.psf.update.base.UpdateParam;
import com.tencent.angel.psagent.PSAgentContext;
import com.tencent.angel.psagent.matrix.oplog.cache.RowUpdateSplitUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;

import java.util.ArrayList;
import java.util.List;

public class InitNeighborParam extends UpdateParam {

  private long[] keys;
  private int[] indptr;
  private long[] neighbors;

  public InitNeighborParam(int matrixId, long[] keys,
                           int[] indptr, long[] neighbors) {
    super(matrixId);
    this.keys = keys;
    this.indptr = indptr;
    this.neighbors = neighbors;
  }

  @Override
  public List<PartitionUpdateParam> split() {
    LongIndexComparator comparator = new LongIndexComparator(keys);
    int[] index = new int[keys.length];
    for (int i = 0; i < index.length; i++)
      index[i] = i;
    IntArrays.quickSort(index, comparator);

    List<PartitionUpdateParam> params = new ArrayList<>();
    List<PartitionKey> parts = PSAgentContext.get().getMatrixMetaManager().getPartitions(matrixId);


    if (!RowUpdateSplitUtils.isInRange(keys, index, parts)) {
      throw new AngelException(
        "node id is not in range [" + parts.get(0).getStartCol() + ", " + parts
          .get(parts.size() - 1).getEndCol());
    }

    int nodeIndex = 0;
    int partIndex = 0;
    while (nodeIndex < index.length || partIndex < parts.size()) {
      int length = 0;
      long endOffset = parts.get(partIndex).getEndCol();
      while (nodeIndex < index.length && keys[index[nodeIndex]] < endOffset) {
        nodeIndex++;
        length++;
      }

      if (length > 0)
        params.add(new PartInitNeighborParam(matrixId,
          parts.get(partIndex), keys, index, indptr, neighbors, nodeIndex - length, nodeIndex));

      partIndex++;
    }

    return params;
  }
}
