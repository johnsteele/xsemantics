/*
 * generated by Xtext
 */
package it.xsemantics.example.fjcached;

import it.xsemantics.example.fj.typing.FjTypeSystem;
import it.xsemantics.example.fjcached.typing.FjCachedTypeSystem;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class FjcachedRuntimeModule extends it.xsemantics.example.fjcached.AbstractFjcachedRuntimeModule {

	public Class<? extends FjTypeSystem> bindFjTypeSystem() {
		return FjCachedTypeSystem.class;
	}
}
