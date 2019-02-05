package engine.multithread;

enum ThreadState {
	GENERAL,
	RESOURCE,
	GPU,
	FREE,
	SYNC,
	TERMINATE;
}
