#include <stdio.h>
#include <stdlib.h>

/*
 * A super simple implementation of the FUNC VM
 * 
 * Compile with 
 *   gcc -o fvm fvm.c
 *
 * Run code with
 *   fvm sample.fvm
 *
 */

/* these need to  match what the compiler produces */

enum opcode {
  OP_STOP = 0,
  OP_PUSH = 1, 
  OP_JUMP = 2, 
  OP_JUMP_TRUE = 3, 
  OP_OPEN = 4, 
  OP_ENV = 5, 
  OP_PUSH_ENV = 6,
  OP_CLOSURE = 7, 
  OP_ADD_ENV = 8, 
  OP_LOOKUP = 9, 
  OP_PRIM_CALL = 10, 
  OP_NOP = 11, 
  OP_SWAP = 12
};

enum primitives {
  PRIM_TIMES = 1, 
  PRIM_PLUS = 2, 
  PRIM_EQ = 3, 
  PRIM_LESS = 4, 
  PRIM_PRINT = 5,
  PRIM_MINUS = 6, 
  PRIM_NOT = 7
};

int memSize = 100000;
int stackPtr;
int *stack;
int envPtr;
int *environments;
int closurePtr;
int *closures;

void push (int v) {
  if (stackPtr >= memSize) {
    printf("Out of stack memory!\n");
    exit(1);
  }
  stack[stackPtr] = v;
  stackPtr++;
}

int pop () {
  stackPtr--;
  return stack[stackPtr];
}

int allocateEnv (int v, int rest) {
  if (envPtr >= memSize) {
    printf("Out of environment memory!\n");
    exit(1);
  }
  environments[envPtr] = v;
  environments[envPtr + 1] = rest;
  envPtr += 2;
  return envPtr - 2;
}

int lookupEnv (int i, int env) {
  int curr = env;
  while (i > 0) {
    curr = environments[curr + 1];
    i--;
  }
  return environments[curr];
}

int allocateClosure (int a, int env) {
  if (closurePtr >= memSize) {
    printf("Out of closure memory!\n");
    exit(1);
  }
  closures[closurePtr] = a;
  closures[closurePtr + 1] = env;
  closurePtr += 2;
  return closurePtr - 2;
}

int main(int argc, char *argv[]) {
  if (argc < 2) {
    printf("USAGE: fvm <file.fvm>\n");
    return 1;
  }
  
  FILE *fp;
  fp = fopen(argv[1], "r");
  char line[255];
  int code[10000];
  int curr = 0;
  while(fgets(line, 255, fp)) {
    if (line[0] == '#') {
      continue;
    }
    code[curr++] = atoi(line);
  }
  printf("[%d opcodes read]\n", curr);
  fclose(fp);

  stack = malloc(sizeof(int) * memSize);
  environments = malloc(sizeof(int) * memSize);
  closures = malloc(sizeof(int) * memSize);
  int PC = 0;
  int ENV = -1;

  int v1, v2, addr;
  
  while(1) {
    /* debugging info - uncomment to see */
    /*
    printf("[PC = %d  opcode = %d  stackPtr = %d  envPtr = %d  closurePtr = %d]\n", PC, code[PC], stackPtr, envPtr, closurePtr);
    printf("%s", "["); for (int i = 0; i < stackPtr; i++) { printf(" %d", stack[i]); } printf("%s", "]\n");
    */

    switch(code[PC]) {
      
    case OP_STOP:
      printf("[Result: %d]\n", pop());
      return 0;
      
    case OP_PUSH:
      push(code[PC + 1]);
      PC += 2;
      break;

    case OP_JUMP:
      PC = pop();
      break;

    case OP_JUMP_TRUE:
      addr = pop();
      v1 = pop();
      if (v1 != 0) {
	PC = addr;
      } else {
	PC++;
      }
      break;

    case OP_OPEN:
      v1 = pop();
      addr = closures[v1];
      ENV = closures[v1 + 1];
      push(addr);
      PC++;
      break;

    case OP_ENV:
      ENV = pop();
      PC++;
      break;

    case OP_PUSH_ENV:
      push(ENV);
      PC++;
      break;

    case OP_CLOSURE:
      addr = pop();
      v1 = allocateClosure(addr, ENV);
      push(v1);
      PC++;
      break;

    case OP_ADD_ENV:
      v1 = pop();
      ENV = allocateEnv(v1, ENV);
      PC++;
      break;

    case OP_LOOKUP:
      v1 = pop();
      push(lookupEnv(v1, ENV));
      PC++;
      break;

    case OP_PRIM_CALL:
      v1 = pop();
      switch(v1) {
      case PRIM_PLUS:
	v1 = pop();
	v2 = pop();
	push(v1 + v2);
	break;

      case PRIM_TIMES:
	v1 = pop();
	v2 = pop();
	push(v1 * v2);
	break;
	
      case PRIM_EQ:
	v1 = pop();
	v2 = pop();
	push(v1 == v2 ? 1 : 0);
	break;
	
      case PRIM_LESS:
	v2 = pop();
	v1 = pop();
	push(v1 < v2 ? 1 : 0);
	break;
	
      case PRIM_PRINT:
	v1 = pop();
	printf("%d\n", v1);
	push(1);
	break;
	
      case PRIM_MINUS:
	v1 = pop();
	push(-v1);
	break;
	
      case PRIM_NOT:
	v1 = pop();
	push(v1 == 0 ? 1 : 0);
	break;

      default:
	printf("Unknown PRIMOP %d", v1);
	exit(1);
      }
      PC++;
      break;

    case OP_NOP:
      PC++;
      break;

    case OP_SWAP:
      v1 = pop();
      v2 = pop();
      push(v1);
      push(v2);
      PC++;
      break;

    default:
      printf("Unknown OPCODE %d", code[PC]);
      exit(1);
    }
  }
  return 0;
}
