/***********************************************************************************
 * Copyright (C) 2018 /// Project SWG /// www.projectswg.com                       *
 *                                                                                 *
 * This file is part of the ProjectSWG Launcher.                                   *
 *                                                                                 *
 * This program is free software: you can redistribute it and/or modify            *
 * it under the terms of the GNU Affero General Public License as published by     *
 * the Free Software Foundation, either version 3 of the License, or               *
 * (at your option) any later version.                                             *
 *                                                                                 *
 * This program is distributed in the hope that it will be useful,                 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                  *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   *
 * GNU Affero General Public License for more details.                             *
 *                                                                                 *
 * You should have received a copy of the GNU Affero General Public License        *
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.          *
 *                                                                                 *
 ***********************************************************************************/

package com.projectswg.launcher.core.resources.pipeline;

import me.joshlarson.jlcommon.log.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Pipeline {
	
	public static <T> PipelineCompiler<T> compile(String name) {
		return new PipelineCompiler<>(name);
	}
	
	public static <T> PipelineExecutor<T> execute(String name) {
		return new PipelineExecutor<>(name);
	}
	
	public static class PipelineCompiler<T> {
		
		private final List<Predicate<T>> stages;
		private final String name;
		
		public PipelineCompiler(String name) {
			this.stages = new CopyOnWriteArrayList<>();
			this.name = name;
		}
		
		public PipelineCompiler<T> next(Predicate<T> stage) {
			stages.add(stage);
			return this;
		}
		
		public PipelineCompiler<T> next(Consumer<T> stage) {
			return next(in -> {stage.accept(in); return true; });
		}
		
		public PipelineCompiler<T> next(Runnable stage) {
			return next(in -> { stage.run(); return true; });
		}
		
		public void execute(T input) {
			int stageIndex = 0;
			try {
				for (Predicate<T> stage : stages) {
					if (!stage.test(input))
						return;
					stageIndex++;
				}
			} catch (Throwable t) {
				Log.e("Pipeline '%s' failed during stage index %d!", name, stageIndex);
				Log.e(t);
			}
		}
		
	}
	
	public static class PipelineExecutor<T> {
		
		private final String name;
		private boolean terminated;
		
		public PipelineExecutor() {
			this("");
		}
		
		public PipelineExecutor(String name) {
			this.name = name;
			this.terminated = false;
		}
		
		public PipelineExecutor<T> execute(Predicate<T> stage, T t) {
			if (terminated)
				return this;
			try {
				stage.test(t);
			} catch (Throwable ex) {
				Log.e("Pipeline '%s' failed!", name);
				Log.e(ex);
				terminated = true;
			}
			return this;
		}
		
		public PipelineExecutor<T> execute(Consumer<T> stage, T t) {
			if (terminated)
				return this;
			try {
				stage.accept(t);
			} catch (Throwable ex) {
				Log.e("Pipeline '%s' failed!", name);
				Log.e(ex);
				terminated = true;
			}
			return this;
		}
		
		public PipelineExecutor<T> execute(Runnable stage) {
			if (terminated)
				return this;
			try {
				stage.run();
			} catch (Throwable t) {
				Log.e("Pipeline '%s' failed!", name);
				Log.e(t);
				terminated = true;
			}
			return this;
		}
		
	}
	
}
