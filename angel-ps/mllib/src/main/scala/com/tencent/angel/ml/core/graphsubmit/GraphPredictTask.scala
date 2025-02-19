/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */


package com.tencent.angel.ml.core.graphsubmit

import com.tencent.angel.ml.core.{AngelEnvContext, PredictTask}
import com.tencent.angel.mlcore.conf.SharedConf
import com.tencent.angel.mlcore.variable.VarState
import com.tencent.angel.ml.math2.utils.LabeledData
import com.tencent.angel.worker.task.TaskContext
import org.apache.hadoop.io.{LongWritable, Text}

class GraphPredictTask(ctx: TaskContext) extends PredictTask[LongWritable, Text](ctx) {

  override def predict(ctx: TaskContext): Unit = {
    val modelClassName: String = sharedConf.modelClassName
    val model: AngelModel = AngelModel(modelClassName, sharedConf, ctx)
    model.buildNetwork()
    model.createMatrices(AngelEnvContext(null))
    predict(ctx, model, taskDataBlock)
  }

  def parse(key: LongWritable, value: Text): LabeledData = {
    dataParser.parse(value.toString)
  }

}
