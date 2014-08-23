#include<stdio.h>
#include<stdlib.h>
#include<stdarg.h>
#include<memory.h>
#include<setjmp.h>
#include<time.h>
#include<z3.h>

#define LOG_Z3_CALLS

#ifdef LOG_Z3_CALLS
#define LOG_MSG(msg) Z3_append_log(msg)
#else
#define LOG_MSG(msg) ((void)0)
#endif

/**
   \defgroup capi_ex C API examples
*/
/*@{*/
/**
   @name Auxiliary Functions
*/
/*@{*/

/**
   \brief exit gracefully in case of error.
*/
void exitf(const char* message)
{
  fprintf(stderr,"BUG: %s.\n", message);
  exit(1);
}

/**
   \brief exit if unreachable code was reached.
*/
void unreachable()
{
    exitf("unreachable code was reached");
}

/**
   \brief Simpler error handler.
 */
void error_handler(Z3_context c, Z3_error_code e)
{
    printf("Error code: %d\n", e);
    exitf("incorrect use of Z3");
}

static jmp_buf g_catch_buffer;
/**
   \brief Low tech exceptions.

   In high-level programming languages, an error handler can throw an exception.
*/
void throw_z3_error(Z3_context c, Z3_error_code e)
{
    longjmp(g_catch_buffer, e);
}

/**
   \brief Display the given type.
*/
void display_sort(Z3_context c, FILE * out, Z3_sort ty)
{
    switch (Z3_get_sort_kind(c, ty)) {
    case Z3_UNINTERPRETED_SORT:
        display_symbol(c, out, Z3_get_sort_name(c, ty));
        break;
    case Z3_BOOL_SORT:
        fprintf(out, "bool");
        break;
    case Z3_INT_SORT:
        fprintf(out, "int");
        break;
    case Z3_REAL_SORT:
        fprintf(out, "real");
        break;
    case Z3_BV_SORT:
        fprintf(out, "bv%d", Z3_get_bv_sort_size(c, ty));
        break;
    case Z3_ARRAY_SORT:
        fprintf(out, "[");
        display_sort(c, out, Z3_get_array_sort_domain(c, ty));
        fprintf(out, "->");
        display_sort(c, out, Z3_get_array_sort_range(c, ty));
        fprintf(out, "]");
        break;
    case Z3_DATATYPE_SORT:
		if (Z3_get_datatype_sort_num_constructors(c, ty) != 1)
		{
			fprintf(out, "%s", Z3_sort_to_string(c,ty));
			break;
		}
		{
        unsigned num_fields = Z3_get_tuple_sort_num_fields(c, ty);
        unsigned i;
        fprintf(out, "(");
        for (i = 0; i < num_fields; i++) {
            Z3_func_decl field = Z3_get_tuple_sort_field_decl(c, ty, i);
            if (i > 0) {
                fprintf(out, ", ");
            }
            display_sort(c, out, Z3_get_range(c, field));
        }
        fprintf(out, ")");
        break;
    }
    default:
        fprintf(out, "unknown[");
        display_symbol(c, out, Z3_get_sort_name(c, ty));
        fprintf(out, "]");
        break;
    }
}

/**
   \brief Display a symbol in the given output stream.
*/
void display_symbol(Z3_context c, FILE * out, Z3_symbol s)
{
    switch (Z3_get_symbol_kind(c, s)) {
    case Z3_INT_SYMBOL:
        fprintf(out, "#%d", Z3_get_symbol_int(c, s));
        break;
    case Z3_STRING_SYMBOL:
        fprintf(out, "%s", Z3_get_symbol_string(c, s));
        break;
    default:
        unreachable();
    }
}

/**
   \brief Create a logical context.

   Enable model construction. Other configuration parameters can be passed in the cfg variable.

   Also enable tracing to stderr and register custom error handler.
*/
Z3_context mk_context_custom(Z3_config cfg, Z3_error_handler err)
{
    Z3_context ctx;

    Z3_set_param_value(cfg, "MODEL", "true");
    ctx = Z3_mk_context(cfg);
    Z3_set_error_handler(ctx, err);

    return ctx;
}

/**
   \brief Create a logical context.

   Enable model construction only.

   Also enable tracing to stderr and register standard error handler.
*/
Z3_context mk_context()
{
    Z3_config  cfg;
    Z3_context ctx;
    cfg = Z3_mk_config();
    Z3_set_param_value(cfg, "MODEL", "true");
    ctx = mk_context_custom(cfg, error_handler);
    return ctx;
}

/**
   \brief Create a logical context.

   Enable fine-grained proof construction.
   Enable model construction.

   Also enable tracing to stderr and register standard error handler.
*/
Z3_context mk_proof_context() {
    Z3_config cfg = Z3_mk_config();
    Z3_context ctx;
    Z3_set_param_value(cfg, "PROOF_MODE", "2");
    ctx = mk_context_custom(cfg, throw_z3_error);
    Z3_del_config(cfg);
    return ctx;
}

/**
   \brief Create a variable using the given name and type.
*/
Z3_ast mk_var(Z3_context ctx, const char * name, Z3_sort ty)
{
    Z3_symbol   s  = Z3_mk_string_symbol(ctx, name);
    return Z3_mk_const(ctx, s, ty);
}

/**
   \brief Create a boolean variable using the given name.
*/
Z3_ast mk_bool_var(Z3_context ctx, const char * name)
{
    Z3_sort ty = Z3_mk_bool_sort(ctx);
    return mk_var(ctx, name, ty);
}

/**
   \brief Create an integer variable using the given name.
*/
Z3_ast mk_int_var(Z3_context ctx, const char * name)
{
    Z3_sort ty = Z3_mk_int_sort(ctx);
    return mk_var(ctx, name, ty);
}

/**
   \brief Create a Z3 integer node using a C int.
*/
Z3_ast mk_int(Z3_context ctx, int v)
{
    Z3_sort ty = Z3_mk_int_sort(ctx);
    return Z3_mk_int(ctx, v, ty);
}

/**
   \brief Create a real variable using the given name.
*/
Z3_ast mk_real_var(Z3_context ctx, const char * name)
{
    Z3_sort ty = Z3_mk_real_sort(ctx);
    return mk_var(ctx, name, ty);
}

/**
   \brief Create the unary function application: <tt>(f x)</tt>.
*/
Z3_ast mk_unary_app(Z3_context ctx, Z3_func_decl f, Z3_ast x)
{
    Z3_ast args[1] = {x};
    return Z3_mk_app(ctx, f, 1, args);
}

/**
   \brief Create the binary function application: <tt>(f x y)</tt>.
*/
Z3_ast mk_binary_app(Z3_context ctx, Z3_func_decl f, Z3_ast x, Z3_ast y)
{
    Z3_ast args[2] = {x, y};
    return Z3_mk_app(ctx, f, 2, args);
}

/**
   \brief Z3 does not support explicitly tuple updates. They can be easily implemented
   as macros. The argument \c t must have tuple type.
   A tuple update is a new tuple where field \c i has value \c new_val, and all
   other fields have the value of the respective field of \c t.

   <tt>update(t, i, new_val)</tt> is equivalent to
   <tt>mk_tuple(proj_0(t), ..., new_val, ..., proj_n(t))</tt>
*/
Z3_ast mk_tuple_update(Z3_context c, Z3_ast t, unsigned i, Z3_ast new_val)
{
    Z3_sort         ty;
    Z3_func_decl   mk_tuple_decl;
    unsigned            num_fields, j;
    Z3_ast *            new_fields;
    Z3_ast              result;

    ty = Z3_get_sort(c, t);

    if (Z3_get_sort_kind(c, ty) != Z3_DATATYPE_SORT) {
        exitf("argument must be a tuple");
    }

    num_fields = Z3_get_tuple_sort_num_fields(c, ty);

    if (i >= num_fields) {
        exitf("invalid tuple update, index is too big");
    }

    new_fields = (Z3_ast*) malloc(sizeof(Z3_ast) * num_fields);
    for (j = 0; j < num_fields; j++) {
        if (i == j) {
            /* use new_val at position i */
            new_fields[j] = new_val;
        }
        else {
            /* use field j of t */
            Z3_func_decl proj_decl = Z3_get_tuple_sort_field_decl(c, ty, j);
            new_fields[j] = mk_unary_app(c, proj_decl, t);
        }
    }
    mk_tuple_decl = Z3_get_tuple_sort_mk_decl(c, ty);
    result = Z3_mk_app(c, mk_tuple_decl, num_fields, new_fields);
    free(new_fields);
    return result;
}


/**
   \brief Check whether the logical context is satisfiable, and compare the result with the expected result.
   If the context is satisfiable, then display the model.
*/
void check(Z3_context ctx, Z3_lbool expected_result)
{
    Z3_model m      = 0;
    Z3_lbool result = Z3_check_and_get_model(ctx, &m);
	FILE *fp = fopen("z3output.txt", "w");
    printf("\nThe checking result is:%i\n", result);
    switch (result) {
    case Z3_L_FALSE:
        printf("unsat\n");
        fprintf(fp, "unsat\n");
        break;
    case Z3_L_UNDEF:
        printf("unknown\n");
        printf("potential model:\n%s\n", Z3_model_to_string(ctx, m));
        fprintf(fp, "unknown potential model:\n%s\n", Z3_model_to_string(ctx, m));
        fclose(fp);
        break;
    case Z3_L_TRUE:
        printf("sat\n%s\n", Z3_model_to_string(ctx, m));
        fprintf(fp, "sat\n%s\n", Z3_model_to_string(ctx, m));
        break;
    }
    fprintf(fp, "\nResult:\n%s\nEND OF Result\n", Z3_statistics_to_string(ctx));
    fclose(fp);
    if (m) {
//    	printf("\ndelete model.");
        Z3_del_model(ctx, m);
    }
    if (result != expected_result) {
//        exitf("unexpected result");
    }
}



/**
   \brief Prove that the constraints already asserted into the logical
   context implies the given formula.  The result of the proof is
   displayed.

   Z3 is a satisfiability checker. So, one can prove \c f by showing
   that <tt>(not f)</tt> is unsatisfiable.

   The context \c ctx is not modified by this function.
*/
void prove(Z3_context ctx, Z3_ast f, Z3_bool is_valid)
{
    Z3_model m;
    Z3_ast   not_f;

    /* save the current state of the context */
    Z3_push(ctx);

    not_f = Z3_mk_not(ctx, f);
    Z3_assert_cnstr(ctx, not_f);

    m = 0;

    switch (Z3_check_and_get_model(ctx, &m)) {
    case Z3_L_FALSE:
        /* proved */
        printf("valid\n");
        if (!is_valid) {
            exitf("unexpected result");
        }
        break;
    case Z3_L_UNDEF:
        /* Z3 failed to prove/disprove f. */
        printf("unknown\n");
        if (m != 0) {
            /* m should be viewed as a potential counterexample. */
            printf("potential counterexample:\n%s\n", Z3_model_to_string(ctx, m));
        }
        if (is_valid) {
            exitf("unexpected result");
        }
        break;
    case Z3_L_TRUE:
        /* disproved */
        printf("invalid\n");
        if (m) {
            /* the model returned by Z3 is a counterexample */
            printf("counterexample:\n%s\n", Z3_model_to_string(ctx, m));
        }
        if (is_valid) {
            exitf("unexpected result");
        }
        break;
    }

    if (m) {
        Z3_del_model(ctx, m);
    }

    /* restore context */
    Z3_pop(ctx, 1);
}

/**
   \brief Assert the axiom: function f is injective in the i-th argument.

   The following axiom is asserted into the logical context:
   \code
   forall (x_0, ..., x_n) finv(f(x_0, ..., x_i, ..., x_{n-1})) = x_i
   \endcode

   Where, \c finv is a fresh function declaration.
*/
void assert_inj_axiom(Z3_context ctx, Z3_func_decl f, unsigned i)
{
    unsigned sz, j;
    Z3_sort finv_domain, finv_range;
    Z3_func_decl finv;
    Z3_sort * types; /* types of the quantified variables */
    Z3_symbol *   names; /* names of the quantified variables */
    Z3_ast * xs;         /* arguments for the application f(x_0, ..., x_i, ..., x_{n-1}) */
    Z3_ast   x_i, fxs, finv_fxs, eq;
    Z3_pattern p;
    Z3_ast   q;
    sz = Z3_get_domain_size(ctx, f);

    if (i >= sz) {
        exitf("failed to create inj axiom");
    }

    /* declare the i-th inverse of f: finv */
    finv_domain = Z3_get_range(ctx, f);
    finv_range  = Z3_get_domain(ctx, f, i);
    finv        = Z3_mk_fresh_func_decl(ctx, "inv", 1, &finv_domain, finv_range);

    /* allocate temporary arrays */
    types       = (Z3_sort *) malloc(sizeof(Z3_sort) * sz);
    names       = (Z3_symbol *) malloc(sizeof(Z3_symbol) * sz);
    xs          = (Z3_ast *) malloc(sizeof(Z3_ast) * sz);

    /* fill types, names and xs */
    for (j = 0; j < sz; j++) { types[j] = Z3_get_domain(ctx, f, j); };
    for (j = 0; j < sz; j++) { names[j] = Z3_mk_int_symbol(ctx, j); };
    for (j = 0; j < sz; j++) { xs[j]    = Z3_mk_bound(ctx, j, types[j]); };

    x_i = xs[i];

    /* create f(x_0, ..., x_i, ..., x_{n-1}) */
    fxs         = Z3_mk_app(ctx, f, sz, xs);

    /* create f_inv(f(x_0, ..., x_i, ..., x_{n-1})) */
    finv_fxs    = mk_unary_app(ctx, finv, fxs);

    /* create finv(f(x_0, ..., x_i, ..., x_{n-1})) = x_i */
    eq          = Z3_mk_eq(ctx, finv_fxs, x_i);

    /* use f(x_0, ..., x_i, ..., x_{n-1}) as the pattern for the quantifier */
    p           = Z3_mk_pattern(ctx, 1, &fxs);
    printf("pattern: %s\n", Z3_pattern_to_string(ctx, p));
    printf("\n");

    /* create & assert quantifier */
    q           = Z3_mk_forall(ctx,
                                 0, /* using default weight */
                                 1, /* number of patterns */
                                 &p, /* address of the "array" of patterns */
                                 sz, /* number of quantified variables */
                                 types,
                                 names,
                                 eq);
    printf("assert axiom:\n%s\n", Z3_ast_to_string(ctx, q));
    Z3_assert_cnstr(ctx, q);

    /* free temporary arrays */
    free(types);
    free(names);
    free(xs);
}

/**
   \brief Assert the axiom: function f is commutative.

   This example uses the SMT-LIB parser to simplify the axiom construction.
*/
void assert_comm_axiom(Z3_context ctx, Z3_func_decl f)
{
    Z3_sort t;
    Z3_symbol f_name, t_name;
    Z3_ast q;

    t = Z3_get_range(ctx, f);

    if (Z3_get_domain_size(ctx, f) != 2 ||
        Z3_get_domain(ctx, f, 0) != t ||
        Z3_get_domain(ctx, f, 1) != t) {
        exitf("function must be binary, and argument types must be equal to return type");
    }

    /* Inside the parser, function f will be referenced using the symbol 'f'. */
    f_name = Z3_mk_string_symbol(ctx, "f");

    /* Inside the parser, type t will be referenced using the symbol 'T'. */
    t_name = Z3_mk_string_symbol(ctx, "T");


    Z3_parse_smtlib_string(ctx,
                           "(benchmark comm :formula (forall (x T) (y T) (= (f x y) (f y x))))",
                           1, &t_name, &t,
                           1, &f_name, &f);
    q = Z3_get_smtlib_formula(ctx, 0);
    printf("assert axiom:\n%s\n", Z3_ast_to_string(ctx, q));
    Z3_assert_cnstr(ctx, q);
}

int encode(int i){

	return 0;
}

int *decode(int d){

	return 0;
}

/**
 * Jan 22nd 2013
 */
Z3_ast mk_add(Z3_context ctx, Z3_ast left, Z3_ast right){
	Z3_ast toSum[2] = {left, right};
	return Z3_mk_add(ctx, 2, toSum);
}

Z3_ast mk_mul(Z3_context ctx, Z3_ast left, Z3_ast right){
	Z3_ast toSum[2] = {left, right};
	return Z3_mk_mul(ctx, 2, toSum);
}

Z3_ast mk_sub(Z3_context ctx, Z3_ast left, Z3_ast right){
	Z3_ast toMinus[2] = {left, right};
	return Z3_mk_sub(ctx, 2, toMinus);
}

Z3_ast mk_div(Z3_context ctx, Z3_ast left, Z3_ast right){
	return Z3_mk_div(ctx, left, right);
}

Z3_ast mk_mod(Z3_context ctx, Z3_ast left, Z3_ast right){
	return Z3_mk_mod(ctx, left, right);
}

Z3_ast mk_and(Z3_context ctx, Z3_ast left, Z3_ast right){
	Z3_ast toAnd[2] = {left, right};
	return Z3_mk_and(ctx, 2, toAnd);
}

Z3_ast mk_or(Z3_context ctx, Z3_ast left, Z3_ast right){
	Z3_ast toOr[2] = {left, right};
	return Z3_mk_or(ctx, 2, toOr);
}

void nullInductionChecker(Z3_context ctx) {
LOG_MSG("Test a high level Petri net, unrolled transitions");
printf("Test a high level Petri net, unrolled transitions");
//function declarations
Z3_func_decl mk_tuple_decl, proj_decls[4];
Z3_symbol names[4];
Z3_sort sorts[4];
names[0] = Z3_mk_string_symbol(ctx, "Idle");
names[1] = Z3_mk_string_symbol(ctx, "Trying");
names[2] = Z3_mk_string_symbol(ctx, "Critical");
names[3] = Z3_mk_string_symbol(ctx, "Monitor");
Z3_func_decl DT0_mk_tuple_decl;
Z3_func_decl DT0_proj_decls[1];
Z3_symbol DT0_names[1];
Z3_sort DT0_sorts[1];
DT0_names[0] = Z3_mk_string_symbol(ctx, "DT0f0");
DT0_sorts[0] = Z3_mk_int_sort(ctx);
Z3_sort DT0SORT = Z3_mk_tuple_sort(ctx, Z3_mk_string_symbol(ctx, "DT0SORT"), 1, DT0_names, DT0_sorts, &DT0_mk_tuple_decl, DT0_proj_decls);
Z3_func_decl DT1_mk_tuple_decl;
Z3_func_decl DT1_proj_decls[2];
Z3_symbol DT1_names[2];
Z3_sort DT1_sorts[2];
DT1_names[0] = Z3_mk_string_symbol(ctx, "DT1f0");
DT1_sorts[0] = Z3_mk_int_sort(ctx);
DT1_names[1] = Z3_mk_string_symbol(ctx, "DT1f1");
DT1_sorts[1] = Z3_mk_int_sort(ctx);
Z3_sort DT1SORT = Z3_mk_tuple_sort(ctx, Z3_mk_string_symbol(ctx, "DT1SORT"), 2, DT1_names, DT1_sorts, &DT1_mk_tuple_decl, DT1_proj_decls);
sorts[0] = Z3_mk_set_sort(ctx, DT0SORT);
sorts[1] = Z3_mk_set_sort(ctx, DT0SORT);
sorts[2] = Z3_mk_set_sort(ctx, DT0SORT);
sorts[3] = Z3_mk_set_sort(ctx, DT1SORT);
Z3_sort STATE_TUPLE = Z3_mk_tuple_sort(ctx, Z3_mk_string_symbol(ctx, "State"), 4, names, sorts, &mk_tuple_decl, proj_decls);
//build all depth number of states
Z3_ast S0 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0"), STATE_TUPLE);
Z3_ast S1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1"), STATE_TUPLE);
Z3_ast S2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2"), STATE_TUPLE);
Z3_ast S3 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S3"), STATE_TUPLE);
//transitions
Z3_ast transitions_and[3];

Z3_ast S0_trans_or[5];
Z3_ast S0_getTicket_Idle_gt = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0gt"), DT0SORT);
Z3_ast S0_getTicket_Monitor_M1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0M1"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S0_getTicket_Trying_ot = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0ot"), DT0SORT);
Z3_ast S0_getTicket_Monitor_M2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0M2"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S0_getTicket_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0Monitorx"), DT1SORT);
Z3_ast S0_getTicket_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0Monitory"), DT1SORT);
Z3_ast S0temp_0 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "temp_0"), DT1SORT);
Z3_ast t0S0_cond_and[7];
t0S0_cond_and[0] = Z3_mk_set_member(ctx, S0_getTicket_Idle_gt, mk_unary_app(ctx, proj_decls[0], S0));
t0S0_cond_and[1] = Z3_mk_eq(ctx, S0_getTicket_Monitor_M1, mk_unary_app(ctx, proj_decls[3], S0));
t0S0_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0_getTicket_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S0_getTicket_Idle_gt)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0_getTicket_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S0_getTicket_Idle_gt)))), Z3_mk_eq(ctx, S0_getTicket_Trying_ot, S0_getTicket_Idle_gt)), Z3_mk_eq(ctx, S0_getTicket_Monitor_M2, Z3_mk_set_add(ctx, Z3_mk_set_del(ctx, S0_getTicket_Monitor_M1, S0_getTicket_Monitor_x), S0temp_0)));
t0S0_cond_and[3] = Z3_mk_set_member(ctx, S0_getTicket_Monitor_x, S0_getTicket_Monitor_M1);
t0S0_cond_and[4] = Z3_mk_set_member(ctx, S0_getTicket_Monitor_y, S0_getTicket_Monitor_M1);
t0S0_cond_and[5] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0temp_0), mk_unary_app(ctx, DT1_proj_decls[0], S0_getTicket_Monitor_x));
t0S0_cond_and[6] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S0temp_0), mk_add(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S0_getTicket_Monitor_y), mk_int(ctx, 1)));
Z3_ast t0S0_cond = Z3_mk_and(ctx, 7, t0S0_cond_and);
Z3_ast t0S0_true_and[4];
t0S0_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[0], S0), S0_getTicket_Idle_gt));
t0S0_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[1], S0), S0_getTicket_Trying_ot));
t0S0_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), S0_getTicket_Monitor_M2);
t0S0_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), mk_unary_app(ctx, proj_decls[2], S0));
Z3_ast t0S0_true = Z3_mk_and(ctx, 4, t0S0_true_and);
Z3_ast t0S0_false_and[4];
t0S0_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), mk_unary_app(ctx, proj_decls[0], S0));
t0S0_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), mk_unary_app(ctx, proj_decls[1], S0));
t0S0_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), mk_unary_app(ctx, proj_decls[2], S0));
t0S0_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), mk_unary_app(ctx, proj_decls[3], S0));
Z3_ast t0S0_false = Z3_mk_and(ctx, 4, t0S0_false_and);
Z3_ast t0S0 = Z3_mk_ite(ctx, t0S0_cond, t0S0_true, t0S0_false);
S0_trans_or[0] = t0S0;
Z3_ast S0_enterCritical_1_Trying_e1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0e1"), DT0SORT);
Z3_ast S0_enterCritical_1_Monitor_M3 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0M3"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S0_enterCritical_1_Critical_c1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0c1"), DT0SORT);
Z3_ast S0_enterCritical_1_Monitor_M4 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0M4"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S0_enterCritical_1_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0Monitorx"), DT1SORT);
Z3_ast S0_enterCritical_1_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0Monitory"), DT1SORT);
Z3_ast t1S0_cond_and[5];
t1S0_cond_and[0] = Z3_mk_set_member(ctx, S0_enterCritical_1_Trying_e1, mk_unary_app(ctx, proj_decls[1], S0));
t1S0_cond_and[1] = Z3_mk_eq(ctx, S0_enterCritical_1_Monitor_M3, mk_unary_app(ctx, proj_decls[3], S0));
t1S0_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0_enterCritical_1_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S0_enterCritical_1_Trying_e1)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0_enterCritical_1_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S0_enterCritical_1_Trying_e1)))), Z3_mk_lt(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S0_enterCritical_1_Monitor_x), mk_unary_app(ctx, DT1_proj_decls[1], S0_enterCritical_1_Monitor_y))), Z3_mk_eq(ctx, S0_enterCritical_1_Critical_c1, S0_enterCritical_1_Trying_e1)), Z3_mk_eq(ctx, S0_enterCritical_1_Monitor_M4, S0_enterCritical_1_Monitor_M3));
t1S0_cond_and[3] = Z3_mk_set_member(ctx, S0_enterCritical_1_Monitor_x, S0_enterCritical_1_Monitor_M3);
t1S0_cond_and[4] = Z3_mk_set_member(ctx, S0_enterCritical_1_Monitor_y, S0_enterCritical_1_Monitor_M3);
Z3_ast t1S0_cond = Z3_mk_and(ctx, 5, t1S0_cond_and);
Z3_ast t1S0_true_and[4];
t1S0_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[1], S0), S0_enterCritical_1_Trying_e1));
t1S0_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[2], S0), S0_enterCritical_1_Critical_c1));
t1S0_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), S0_enterCritical_1_Monitor_M4);
t1S0_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), mk_unary_app(ctx, proj_decls[0], S0));
Z3_ast t1S0_true = Z3_mk_and(ctx, 4, t1S0_true_and);
Z3_ast t1S0_false_and[4];
t1S0_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), mk_unary_app(ctx, proj_decls[0], S0));
t1S0_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), mk_unary_app(ctx, proj_decls[1], S0));
t1S0_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), mk_unary_app(ctx, proj_decls[2], S0));
t1S0_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), mk_unary_app(ctx, proj_decls[3], S0));
Z3_ast t1S0_false = Z3_mk_and(ctx, 4, t1S0_false_and);
Z3_ast t1S0 = Z3_mk_ite(ctx, t1S0_cond, t1S0_true, t1S0_false);
S0_trans_or[1] = t1S0;
Z3_ast S0_enterCritical_2_Trying_e2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0e2"), DT0SORT);
Z3_ast S0_enterCritical_2_Monitor_M5 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0M5"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S0_enterCritical_2_Critical_c2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0c2"), DT0SORT);
Z3_ast S0_enterCritical_2_Monitor_M6 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0M6"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S0_enterCritical_2_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0Monitorx"), DT1SORT);
Z3_ast S0_enterCritical_2_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0Monitory"), DT1SORT);
Z3_ast t2S0_cond_and[5];
t2S0_cond_and[0] = Z3_mk_set_member(ctx, S0_enterCritical_2_Trying_e2, mk_unary_app(ctx, proj_decls[1], S0));
t2S0_cond_and[1] = Z3_mk_eq(ctx, S0_enterCritical_2_Monitor_M5, mk_unary_app(ctx, proj_decls[3], S0));
t2S0_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0_enterCritical_2_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S0_enterCritical_2_Trying_e2)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0_enterCritical_2_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S0_enterCritical_2_Trying_e2)))), Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S0_enterCritical_2_Monitor_y), mk_int(ctx, 0))), Z3_mk_eq(ctx, S0_enterCritical_2_Critical_c2, S0_enterCritical_2_Trying_e2)), Z3_mk_eq(ctx, S0_enterCritical_2_Monitor_M6, S0_enterCritical_2_Monitor_M5));
t2S0_cond_and[3] = Z3_mk_set_member(ctx, S0_enterCritical_2_Monitor_x, S0_enterCritical_2_Monitor_M5);
t2S0_cond_and[4] = Z3_mk_set_member(ctx, S0_enterCritical_2_Monitor_y, S0_enterCritical_2_Monitor_M5);
Z3_ast t2S0_cond = Z3_mk_and(ctx, 5, t2S0_cond_and);
Z3_ast t2S0_true_and[4];
t2S0_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[1], S0), S0_enterCritical_2_Trying_e2));
t2S0_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[2], S0), S0_enterCritical_2_Critical_c2));
t2S0_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), S0_enterCritical_2_Monitor_M6);
t2S0_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), mk_unary_app(ctx, proj_decls[0], S0));
Z3_ast t2S0_true = Z3_mk_and(ctx, 4, t2S0_true_and);
Z3_ast t2S0_false_and[4];
t2S0_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), mk_unary_app(ctx, proj_decls[0], S0));
t2S0_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), mk_unary_app(ctx, proj_decls[1], S0));
t2S0_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), mk_unary_app(ctx, proj_decls[2], S0));
t2S0_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), mk_unary_app(ctx, proj_decls[3], S0));
Z3_ast t2S0_false = Z3_mk_and(ctx, 4, t2S0_false_and);
Z3_ast t2S0 = Z3_mk_ite(ctx, t2S0_cond, t2S0_true, t2S0_false);
S0_trans_or[2] = t2S0;
Z3_ast S0_outCritical_Critical_co = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0co"), DT0SORT);
Z3_ast S0_outCritical_Monitor_M7 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0M7"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S0_outCritical_Idle_i = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0i"), DT0SORT);
Z3_ast S0_outCritical_Monitor_M8 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0M8"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S0_outCritical_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S0Monitorx"), DT1SORT);
Z3_ast S0temp_1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "temp_1"), DT1SORT);
Z3_ast t3S0_cond_and[6];
t3S0_cond_and[0] = Z3_mk_set_member(ctx, S0_outCritical_Critical_co, mk_unary_app(ctx, proj_decls[2], S0));
t3S0_cond_and[1] = Z3_mk_eq(ctx, S0_outCritical_Monitor_M7, mk_unary_app(ctx, proj_decls[3], S0));
t3S0_cond_and[2] = mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0_outCritical_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S0_outCritical_Critical_co)), Z3_mk_eq(ctx, S0_outCritical_Idle_i, S0_outCritical_Critical_co)), Z3_mk_eq(ctx, S0_outCritical_Monitor_M8, Z3_mk_set_add(ctx, Z3_mk_set_del(ctx, S0_outCritical_Monitor_M7, S0_outCritical_Monitor_x), S0temp_1)));
t3S0_cond_and[3] = Z3_mk_set_member(ctx, S0_outCritical_Monitor_x, S0_outCritical_Monitor_M7);
t3S0_cond_and[4] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S0temp_1), mk_unary_app(ctx, DT1_proj_decls[0], S0_outCritical_Monitor_x));
t3S0_cond_and[5] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S0temp_1), mk_int(ctx, 0));
Z3_ast t3S0_cond = Z3_mk_and(ctx, 6, t3S0_cond_and);
Z3_ast t3S0_true_and[4];
t3S0_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[2], S0), S0_outCritical_Critical_co));
t3S0_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[0], S0), S0_outCritical_Idle_i));
t3S0_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), S0_outCritical_Monitor_M8);
t3S0_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), mk_unary_app(ctx, proj_decls[1], S0));
Z3_ast t3S0_true = Z3_mk_and(ctx, 4, t3S0_true_and);
Z3_ast t3S0_false_and[4];
t3S0_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), mk_unary_app(ctx, proj_decls[0], S0));
t3S0_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), mk_unary_app(ctx, proj_decls[1], S0));
t3S0_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), mk_unary_app(ctx, proj_decls[2], S0));
t3S0_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), mk_unary_app(ctx, proj_decls[3], S0));
Z3_ast t3S0_false = Z3_mk_and(ctx, 4, t3S0_false_and);
Z3_ast t3S0 = Z3_mk_ite(ctx, t3S0_cond, t3S0_true, t3S0_false);
S0_trans_or[3] = t3S0;
Z3_ast tDumpS0_and[4];
tDumpS0_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S1), mk_unary_app(ctx, proj_decls[0], S0));
tDumpS0_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S1), mk_unary_app(ctx, proj_decls[1], S0));
tDumpS0_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S1), mk_unary_app(ctx, proj_decls[2], S0));
tDumpS0_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S1), mk_unary_app(ctx, proj_decls[3], S0));
Z3_ast tDumpS0 = Z3_mk_and(ctx, 4, tDumpS0_and);
S0_trans_or[4] = Z3_mk_implies(ctx, Z3_mk_true(ctx), tDumpS0);
Z3_ast BigTrans_S0 = Z3_mk_or(ctx, 5, S0_trans_or);
transitions_and[0] = BigTrans_S0;

Z3_ast S1_trans_or[5];
Z3_ast S1_getTicket_Idle_gt = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1gt"), DT0SORT);
Z3_ast S1_getTicket_Monitor_M1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1M1"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S1_getTicket_Trying_ot = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1ot"), DT0SORT);
Z3_ast S1_getTicket_Monitor_M2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1M2"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S1_getTicket_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1Monitorx"), DT1SORT);
Z3_ast S1_getTicket_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1Monitory"), DT1SORT);
Z3_ast S1temp_2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "temp_2"), DT1SORT);
Z3_ast t0S1_cond_and[7];
t0S1_cond_and[0] = Z3_mk_set_member(ctx, S1_getTicket_Idle_gt, mk_unary_app(ctx, proj_decls[0], S1));
t0S1_cond_and[1] = Z3_mk_eq(ctx, S1_getTicket_Monitor_M1, mk_unary_app(ctx, proj_decls[3], S1));
t0S1_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1_getTicket_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S1_getTicket_Idle_gt)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1_getTicket_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S1_getTicket_Idle_gt)))), Z3_mk_eq(ctx, S1_getTicket_Trying_ot, S1_getTicket_Idle_gt)), Z3_mk_eq(ctx, S1_getTicket_Monitor_M2, Z3_mk_set_add(ctx, Z3_mk_set_del(ctx, S1_getTicket_Monitor_M1, S1_getTicket_Monitor_x), S1temp_2)));
t0S1_cond_and[3] = Z3_mk_set_member(ctx, S1_getTicket_Monitor_x, S1_getTicket_Monitor_M1);
t0S1_cond_and[4] = Z3_mk_set_member(ctx, S1_getTicket_Monitor_y, S1_getTicket_Monitor_M1);
t0S1_cond_and[5] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1temp_2), mk_unary_app(ctx, DT1_proj_decls[0], S1_getTicket_Monitor_x));
t0S1_cond_and[6] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S1temp_2), mk_add(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S1_getTicket_Monitor_y), mk_int(ctx, 1)));
Z3_ast t0S1_cond = Z3_mk_and(ctx, 7, t0S1_cond_and);
Z3_ast t0S1_true_and[4];
t0S1_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[0], S1), S1_getTicket_Idle_gt));
t0S1_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[1], S1), S1_getTicket_Trying_ot));
t0S1_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), S1_getTicket_Monitor_M2);
t0S1_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), mk_unary_app(ctx, proj_decls[2], S1));
Z3_ast t0S1_true = Z3_mk_and(ctx, 4, t0S1_true_and);
Z3_ast t0S1_false_and[4];
t0S1_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), mk_unary_app(ctx, proj_decls[0], S1));
t0S1_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), mk_unary_app(ctx, proj_decls[1], S1));
t0S1_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), mk_unary_app(ctx, proj_decls[2], S1));
t0S1_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), mk_unary_app(ctx, proj_decls[3], S1));
Z3_ast t0S1_false = Z3_mk_and(ctx, 4, t0S1_false_and);
Z3_ast t0S1 = Z3_mk_ite(ctx, t0S1_cond, t0S1_true, t0S1_false);
S1_trans_or[0] = t0S1;
Z3_ast S1_enterCritical_1_Trying_e1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1e1"), DT0SORT);
Z3_ast S1_enterCritical_1_Monitor_M3 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1M3"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S1_enterCritical_1_Critical_c1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1c1"), DT0SORT);
Z3_ast S1_enterCritical_1_Monitor_M4 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1M4"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S1_enterCritical_1_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1Monitorx"), DT1SORT);
Z3_ast S1_enterCritical_1_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1Monitory"), DT1SORT);
Z3_ast t1S1_cond_and[5];
t1S1_cond_and[0] = Z3_mk_set_member(ctx, S1_enterCritical_1_Trying_e1, mk_unary_app(ctx, proj_decls[1], S1));
t1S1_cond_and[1] = Z3_mk_eq(ctx, S1_enterCritical_1_Monitor_M3, mk_unary_app(ctx, proj_decls[3], S1));
t1S1_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1_enterCritical_1_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S1_enterCritical_1_Trying_e1)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1_enterCritical_1_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S1_enterCritical_1_Trying_e1)))), Z3_mk_lt(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S1_enterCritical_1_Monitor_x), mk_unary_app(ctx, DT1_proj_decls[1], S1_enterCritical_1_Monitor_y))), Z3_mk_eq(ctx, S1_enterCritical_1_Critical_c1, S1_enterCritical_1_Trying_e1)), Z3_mk_eq(ctx, S1_enterCritical_1_Monitor_M4, S1_enterCritical_1_Monitor_M3));
t1S1_cond_and[3] = Z3_mk_set_member(ctx, S1_enterCritical_1_Monitor_x, S1_enterCritical_1_Monitor_M3);
t1S1_cond_and[4] = Z3_mk_set_member(ctx, S1_enterCritical_1_Monitor_y, S1_enterCritical_1_Monitor_M3);
Z3_ast t1S1_cond = Z3_mk_and(ctx, 5, t1S1_cond_and);
Z3_ast t1S1_true_and[4];
t1S1_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[1], S1), S1_enterCritical_1_Trying_e1));
t1S1_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[2], S1), S1_enterCritical_1_Critical_c1));
t1S1_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), S1_enterCritical_1_Monitor_M4);
t1S1_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), mk_unary_app(ctx, proj_decls[0], S1));
Z3_ast t1S1_true = Z3_mk_and(ctx, 4, t1S1_true_and);
Z3_ast t1S1_false_and[4];
t1S1_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), mk_unary_app(ctx, proj_decls[0], S1));
t1S1_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), mk_unary_app(ctx, proj_decls[1], S1));
t1S1_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), mk_unary_app(ctx, proj_decls[2], S1));
t1S1_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), mk_unary_app(ctx, proj_decls[3], S1));
Z3_ast t1S1_false = Z3_mk_and(ctx, 4, t1S1_false_and);
Z3_ast t1S1 = Z3_mk_ite(ctx, t1S1_cond, t1S1_true, t1S1_false);
S1_trans_or[1] = t1S1;
Z3_ast S1_enterCritical_2_Trying_e2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1e2"), DT0SORT);
Z3_ast S1_enterCritical_2_Monitor_M5 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1M5"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S1_enterCritical_2_Critical_c2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1c2"), DT0SORT);
Z3_ast S1_enterCritical_2_Monitor_M6 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1M6"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S1_enterCritical_2_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1Monitorx"), DT1SORT);
Z3_ast S1_enterCritical_2_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1Monitory"), DT1SORT);
Z3_ast t2S1_cond_and[5];
t2S1_cond_and[0] = Z3_mk_set_member(ctx, S1_enterCritical_2_Trying_e2, mk_unary_app(ctx, proj_decls[1], S1));
t2S1_cond_and[1] = Z3_mk_eq(ctx, S1_enterCritical_2_Monitor_M5, mk_unary_app(ctx, proj_decls[3], S1));
t2S1_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1_enterCritical_2_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S1_enterCritical_2_Trying_e2)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1_enterCritical_2_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S1_enterCritical_2_Trying_e2)))), Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S1_enterCritical_2_Monitor_y), mk_int(ctx, 0))), Z3_mk_eq(ctx, S1_enterCritical_2_Critical_c2, S1_enterCritical_2_Trying_e2)), Z3_mk_eq(ctx, S1_enterCritical_2_Monitor_M6, S1_enterCritical_2_Monitor_M5));
t2S1_cond_and[3] = Z3_mk_set_member(ctx, S1_enterCritical_2_Monitor_x, S1_enterCritical_2_Monitor_M5);
t2S1_cond_and[4] = Z3_mk_set_member(ctx, S1_enterCritical_2_Monitor_y, S1_enterCritical_2_Monitor_M5);
Z3_ast t2S1_cond = Z3_mk_and(ctx, 5, t2S1_cond_and);
Z3_ast t2S1_true_and[4];
t2S1_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[1], S1), S1_enterCritical_2_Trying_e2));
t2S1_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[2], S1), S1_enterCritical_2_Critical_c2));
t2S1_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), S1_enterCritical_2_Monitor_M6);
t2S1_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), mk_unary_app(ctx, proj_decls[0], S1));
Z3_ast t2S1_true = Z3_mk_and(ctx, 4, t2S1_true_and);
Z3_ast t2S1_false_and[4];
t2S1_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), mk_unary_app(ctx, proj_decls[0], S1));
t2S1_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), mk_unary_app(ctx, proj_decls[1], S1));
t2S1_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), mk_unary_app(ctx, proj_decls[2], S1));
t2S1_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), mk_unary_app(ctx, proj_decls[3], S1));
Z3_ast t2S1_false = Z3_mk_and(ctx, 4, t2S1_false_and);
Z3_ast t2S1 = Z3_mk_ite(ctx, t2S1_cond, t2S1_true, t2S1_false);
S1_trans_or[2] = t2S1;
Z3_ast S1_outCritical_Critical_co = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1co"), DT0SORT);
Z3_ast S1_outCritical_Monitor_M7 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1M7"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S1_outCritical_Idle_i = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1i"), DT0SORT);
Z3_ast S1_outCritical_Monitor_M8 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1M8"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S1_outCritical_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S1Monitorx"), DT1SORT);
Z3_ast S1temp_3 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "temp_3"), DT1SORT);
Z3_ast t3S1_cond_and[6];
t3S1_cond_and[0] = Z3_mk_set_member(ctx, S1_outCritical_Critical_co, mk_unary_app(ctx, proj_decls[2], S1));
t3S1_cond_and[1] = Z3_mk_eq(ctx, S1_outCritical_Monitor_M7, mk_unary_app(ctx, proj_decls[3], S1));
t3S1_cond_and[2] = mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1_outCritical_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S1_outCritical_Critical_co)), Z3_mk_eq(ctx, S1_outCritical_Idle_i, S1_outCritical_Critical_co)), Z3_mk_eq(ctx, S1_outCritical_Monitor_M8, Z3_mk_set_add(ctx, Z3_mk_set_del(ctx, S1_outCritical_Monitor_M7, S1_outCritical_Monitor_x), S1temp_3)));
t3S1_cond_and[3] = Z3_mk_set_member(ctx, S1_outCritical_Monitor_x, S1_outCritical_Monitor_M7);
t3S1_cond_and[4] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S1temp_3), mk_unary_app(ctx, DT1_proj_decls[0], S1_outCritical_Monitor_x));
t3S1_cond_and[5] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S1temp_3), mk_int(ctx, 0));
Z3_ast t3S1_cond = Z3_mk_and(ctx, 6, t3S1_cond_and);
Z3_ast t3S1_true_and[4];
t3S1_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[2], S1), S1_outCritical_Critical_co));
t3S1_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[0], S1), S1_outCritical_Idle_i));
t3S1_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), S1_outCritical_Monitor_M8);
t3S1_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), mk_unary_app(ctx, proj_decls[1], S1));
Z3_ast t3S1_true = Z3_mk_and(ctx, 4, t3S1_true_and);
Z3_ast t3S1_false_and[4];
t3S1_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), mk_unary_app(ctx, proj_decls[0], S1));
t3S1_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), mk_unary_app(ctx, proj_decls[1], S1));
t3S1_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), mk_unary_app(ctx, proj_decls[2], S1));
t3S1_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), mk_unary_app(ctx, proj_decls[3], S1));
Z3_ast t3S1_false = Z3_mk_and(ctx, 4, t3S1_false_and);
Z3_ast t3S1 = Z3_mk_ite(ctx, t3S1_cond, t3S1_true, t3S1_false);
S1_trans_or[3] = t3S1;
Z3_ast tDumpS1_and[4];
tDumpS1_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S2), mk_unary_app(ctx, proj_decls[0], S1));
tDumpS1_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S2), mk_unary_app(ctx, proj_decls[1], S1));
tDumpS1_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S2), mk_unary_app(ctx, proj_decls[2], S1));
tDumpS1_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S2), mk_unary_app(ctx, proj_decls[3], S1));
Z3_ast tDumpS1 = Z3_mk_and(ctx, 4, tDumpS1_and);
S1_trans_or[4] = Z3_mk_implies(ctx, Z3_mk_true(ctx), tDumpS1);
Z3_ast BigTrans_S1 = Z3_mk_or(ctx, 5, S1_trans_or);
transitions_and[1] = BigTrans_S1;

Z3_ast S2_trans_or[5];
Z3_ast S2_getTicket_Idle_gt = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2gt"), DT0SORT);
Z3_ast S2_getTicket_Monitor_M1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2M1"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S2_getTicket_Trying_ot = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2ot"), DT0SORT);
Z3_ast S2_getTicket_Monitor_M2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2M2"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S2_getTicket_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2Monitorx"), DT1SORT);
Z3_ast S2_getTicket_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2Monitory"), DT1SORT);
Z3_ast S2temp_4 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "temp_4"), DT1SORT);
Z3_ast t0S2_cond_and[7];
t0S2_cond_and[0] = Z3_mk_set_member(ctx, S2_getTicket_Idle_gt, mk_unary_app(ctx, proj_decls[0], S2));
t0S2_cond_and[1] = Z3_mk_eq(ctx, S2_getTicket_Monitor_M1, mk_unary_app(ctx, proj_decls[3], S2));
t0S2_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2_getTicket_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S2_getTicket_Idle_gt)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2_getTicket_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S2_getTicket_Idle_gt)))), Z3_mk_eq(ctx, S2_getTicket_Trying_ot, S2_getTicket_Idle_gt)), Z3_mk_eq(ctx, S2_getTicket_Monitor_M2, Z3_mk_set_add(ctx, Z3_mk_set_del(ctx, S2_getTicket_Monitor_M1, S2_getTicket_Monitor_x), S2temp_4)));
t0S2_cond_and[3] = Z3_mk_set_member(ctx, S2_getTicket_Monitor_x, S2_getTicket_Monitor_M1);
t0S2_cond_and[4] = Z3_mk_set_member(ctx, S2_getTicket_Monitor_y, S2_getTicket_Monitor_M1);
t0S2_cond_and[5] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2temp_4), mk_unary_app(ctx, DT1_proj_decls[0], S2_getTicket_Monitor_x));
t0S2_cond_and[6] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S2temp_4), mk_add(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S2_getTicket_Monitor_y), mk_int(ctx, 1)));
Z3_ast t0S2_cond = Z3_mk_and(ctx, 7, t0S2_cond_and);
Z3_ast t0S2_true_and[4];
t0S2_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[0], S2), S2_getTicket_Idle_gt));
t0S2_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[1], S2), S2_getTicket_Trying_ot));
t0S2_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), S2_getTicket_Monitor_M2);
t0S2_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), mk_unary_app(ctx, proj_decls[2], S2));
Z3_ast t0S2_true = Z3_mk_and(ctx, 4, t0S2_true_and);
Z3_ast t0S2_false_and[4];
t0S2_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), mk_unary_app(ctx, proj_decls[0], S2));
t0S2_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), mk_unary_app(ctx, proj_decls[1], S2));
t0S2_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), mk_unary_app(ctx, proj_decls[2], S2));
t0S2_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), mk_unary_app(ctx, proj_decls[3], S2));
Z3_ast t0S2_false = Z3_mk_and(ctx, 4, t0S2_false_and);
Z3_ast t0S2 = Z3_mk_ite(ctx, t0S2_cond, t0S2_true, t0S2_false);
S2_trans_or[0] = t0S2;
Z3_ast S2_enterCritical_1_Trying_e1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2e1"), DT0SORT);
Z3_ast S2_enterCritical_1_Monitor_M3 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2M3"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S2_enterCritical_1_Critical_c1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2c1"), DT0SORT);
Z3_ast S2_enterCritical_1_Monitor_M4 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2M4"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S2_enterCritical_1_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2Monitorx"), DT1SORT);
Z3_ast S2_enterCritical_1_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2Monitory"), DT1SORT);
Z3_ast t1S2_cond_and[5];
t1S2_cond_and[0] = Z3_mk_set_member(ctx, S2_enterCritical_1_Trying_e1, mk_unary_app(ctx, proj_decls[1], S2));
t1S2_cond_and[1] = Z3_mk_eq(ctx, S2_enterCritical_1_Monitor_M3, mk_unary_app(ctx, proj_decls[3], S2));
t1S2_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2_enterCritical_1_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S2_enterCritical_1_Trying_e1)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2_enterCritical_1_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S2_enterCritical_1_Trying_e1)))), Z3_mk_lt(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S2_enterCritical_1_Monitor_x), mk_unary_app(ctx, DT1_proj_decls[1], S2_enterCritical_1_Monitor_y))), Z3_mk_eq(ctx, S2_enterCritical_1_Critical_c1, S2_enterCritical_1_Trying_e1)), Z3_mk_eq(ctx, S2_enterCritical_1_Monitor_M4, S2_enterCritical_1_Monitor_M3));
t1S2_cond_and[3] = Z3_mk_set_member(ctx, S2_enterCritical_1_Monitor_x, S2_enterCritical_1_Monitor_M3);
t1S2_cond_and[4] = Z3_mk_set_member(ctx, S2_enterCritical_1_Monitor_y, S2_enterCritical_1_Monitor_M3);
Z3_ast t1S2_cond = Z3_mk_and(ctx, 5, t1S2_cond_and);
Z3_ast t1S2_true_and[4];
t1S2_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[1], S2), S2_enterCritical_1_Trying_e1));
t1S2_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[2], S2), S2_enterCritical_1_Critical_c1));
t1S2_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), S2_enterCritical_1_Monitor_M4);
t1S2_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), mk_unary_app(ctx, proj_decls[0], S2));
Z3_ast t1S2_true = Z3_mk_and(ctx, 4, t1S2_true_and);
Z3_ast t1S2_false_and[4];
t1S2_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), mk_unary_app(ctx, proj_decls[0], S2));
t1S2_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), mk_unary_app(ctx, proj_decls[1], S2));
t1S2_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), mk_unary_app(ctx, proj_decls[2], S2));
t1S2_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), mk_unary_app(ctx, proj_decls[3], S2));
Z3_ast t1S2_false = Z3_mk_and(ctx, 4, t1S2_false_and);
Z3_ast t1S2 = Z3_mk_ite(ctx, t1S2_cond, t1S2_true, t1S2_false);
S2_trans_or[1] = t1S2;
Z3_ast S2_enterCritical_2_Trying_e2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2e2"), DT0SORT);
Z3_ast S2_enterCritical_2_Monitor_M5 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2M5"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S2_enterCritical_2_Critical_c2 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2c2"), DT0SORT);
Z3_ast S2_enterCritical_2_Monitor_M6 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2M6"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S2_enterCritical_2_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2Monitorx"), DT1SORT);
Z3_ast S2_enterCritical_2_Monitor_y = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2Monitory"), DT1SORT);
Z3_ast t2S2_cond_and[5];
t2S2_cond_and[0] = Z3_mk_set_member(ctx, S2_enterCritical_2_Trying_e2, mk_unary_app(ctx, proj_decls[1], S2));
t2S2_cond_and[1] = Z3_mk_eq(ctx, S2_enterCritical_2_Monitor_M5, mk_unary_app(ctx, proj_decls[3], S2));
t2S2_cond_and[2] = mk_and(ctx, mk_and(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2_enterCritical_2_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S2_enterCritical_2_Trying_e2)), Z3_mk_not(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2_enterCritical_2_Monitor_y), mk_unary_app(ctx, DT0_proj_decls[0], S2_enterCritical_2_Trying_e2)))), Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S2_enterCritical_2_Monitor_y), mk_int(ctx, 0))), Z3_mk_eq(ctx, S2_enterCritical_2_Critical_c2, S2_enterCritical_2_Trying_e2)), Z3_mk_eq(ctx, S2_enterCritical_2_Monitor_M6, S2_enterCritical_2_Monitor_M5));
t2S2_cond_and[3] = Z3_mk_set_member(ctx, S2_enterCritical_2_Monitor_x, S2_enterCritical_2_Monitor_M5);
t2S2_cond_and[4] = Z3_mk_set_member(ctx, S2_enterCritical_2_Monitor_y, S2_enterCritical_2_Monitor_M5);
Z3_ast t2S2_cond = Z3_mk_and(ctx, 5, t2S2_cond_and);
Z3_ast t2S2_true_and[4];
t2S2_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[1], S2), S2_enterCritical_2_Trying_e2));
t2S2_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[2], S2), S2_enterCritical_2_Critical_c2));
t2S2_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), S2_enterCritical_2_Monitor_M6);
t2S2_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), mk_unary_app(ctx, proj_decls[0], S2));
Z3_ast t2S2_true = Z3_mk_and(ctx, 4, t2S2_true_and);
Z3_ast t2S2_false_and[4];
t2S2_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), mk_unary_app(ctx, proj_decls[0], S2));
t2S2_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), mk_unary_app(ctx, proj_decls[1], S2));
t2S2_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), mk_unary_app(ctx, proj_decls[2], S2));
t2S2_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), mk_unary_app(ctx, proj_decls[3], S2));
Z3_ast t2S2_false = Z3_mk_and(ctx, 4, t2S2_false_and);
Z3_ast t2S2 = Z3_mk_ite(ctx, t2S2_cond, t2S2_true, t2S2_false);
S2_trans_or[2] = t2S2;
Z3_ast S2_outCritical_Critical_co = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2co"), DT0SORT);
Z3_ast S2_outCritical_Monitor_M7 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2M7"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S2_outCritical_Idle_i = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2i"), DT0SORT);
Z3_ast S2_outCritical_Monitor_M8 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2M8"), Z3_mk_set_sort(ctx, DT1SORT));
Z3_ast S2_outCritical_Monitor_x = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "S2Monitorx"), DT1SORT);
Z3_ast S2temp_5 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "temp_5"), DT1SORT);
Z3_ast t3S2_cond_and[6];
t3S2_cond_and[0] = Z3_mk_set_member(ctx, S2_outCritical_Critical_co, mk_unary_app(ctx, proj_decls[2], S2));
t3S2_cond_and[1] = Z3_mk_eq(ctx, S2_outCritical_Monitor_M7, mk_unary_app(ctx, proj_decls[3], S2));
t3S2_cond_and[2] = mk_and(ctx, mk_and(ctx, Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2_outCritical_Monitor_x), mk_unary_app(ctx, DT0_proj_decls[0], S2_outCritical_Critical_co)), Z3_mk_eq(ctx, S2_outCritical_Idle_i, S2_outCritical_Critical_co)), Z3_mk_eq(ctx, S2_outCritical_Monitor_M8, Z3_mk_set_add(ctx, Z3_mk_set_del(ctx, S2_outCritical_Monitor_M7, S2_outCritical_Monitor_x), S2temp_5)));
t3S2_cond_and[3] = Z3_mk_set_member(ctx, S2_outCritical_Monitor_x, S2_outCritical_Monitor_M7);
t3S2_cond_and[4] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[0], S2temp_5), mk_unary_app(ctx, DT1_proj_decls[0], S2_outCritical_Monitor_x));
t3S2_cond_and[5] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT1_proj_decls[1], S2temp_5), mk_int(ctx, 0));
Z3_ast t3S2_cond = Z3_mk_and(ctx, 6, t3S2_cond_and);
Z3_ast t3S2_true_and[4];
t3S2_true_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), Z3_mk_set_del(ctx, mk_unary_app(ctx, proj_decls[2], S2), S2_outCritical_Critical_co));
t3S2_true_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), Z3_mk_set_add(ctx, mk_unary_app(ctx, proj_decls[0], S2), S2_outCritical_Idle_i));
t3S2_true_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), S2_outCritical_Monitor_M8);
t3S2_true_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), mk_unary_app(ctx, proj_decls[1], S2));
Z3_ast t3S2_true = Z3_mk_and(ctx, 4, t3S2_true_and);
Z3_ast t3S2_false_and[4];
t3S2_false_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), mk_unary_app(ctx, proj_decls[0], S2));
t3S2_false_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), mk_unary_app(ctx, proj_decls[1], S2));
t3S2_false_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), mk_unary_app(ctx, proj_decls[2], S2));
t3S2_false_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), mk_unary_app(ctx, proj_decls[3], S2));
Z3_ast t3S2_false = Z3_mk_and(ctx, 4, t3S2_false_and);
Z3_ast t3S2 = Z3_mk_ite(ctx, t3S2_cond, t3S2_true, t3S2_false);
S2_trans_or[3] = t3S2;
Z3_ast tDumpS2_and[4];
tDumpS2_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[0], S3), mk_unary_app(ctx, proj_decls[0], S2));
tDumpS2_and[1] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[1], S3), mk_unary_app(ctx, proj_decls[1], S2));
tDumpS2_and[2] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[2], S3), mk_unary_app(ctx, proj_decls[2], S2));
tDumpS2_and[3] = Z3_mk_eq(ctx, mk_unary_app(ctx, proj_decls[3], S3), mk_unary_app(ctx, proj_decls[3], S2));
Z3_ast tDumpS2 = Z3_mk_and(ctx, 4, tDumpS2_and);
S2_trans_or[4] = Z3_mk_implies(ctx, Z3_mk_true(ctx), tDumpS2);
Z3_ast BigTrans_S2 = Z3_mk_or(ctx, 5, S2_trans_or);
transitions_and[2] = BigTrans_S2;

Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 3, transitions_and));

//loopFree path construction
Z3_ast loopFree_and[4];
loopFree_and[0] = S0;
loopFree_and[1] = S1;
loopFree_and[2] = S2;
loopFree_and[3] = S3;
Z3_assert_cnstr(ctx, Z3_mk_distinct(ctx, 4, loopFree_and));

//properties
Z3_ast property_and[4];
Z3_ast PS0Tok0 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "PS0_Critical_tok0"), DT0SORT);
Z3_ast S0Critical_tok_0_and[1];
S0Critical_tok_0_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT0_proj_decls[0], PS0Tok0), mk_int(ctx, 1));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 1, S0Critical_tok_0_and));
Z3_ast PS0Tok1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "PS0_Critical_tok1"), DT0SORT);
Z3_ast S0Critical_tok_1_and[1];
S0Critical_tok_1_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT0_proj_decls[0], PS0Tok1), mk_int(ctx, 2));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 1, S0Critical_tok_1_and));
property_and[0] = Z3_mk_not(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_true(ctx), Z3_mk_set_member(ctx, PS0Tok0, mk_unary_app(ctx, proj_decls[2], S0))), Z3_mk_set_member(ctx, PS0Tok1, mk_unary_app(ctx, proj_decls[2], S0))));
Z3_ast PS1Tok0 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "PS1_Critical_tok0"), DT0SORT);
Z3_ast S1Critical_tok_0_and[1];
S1Critical_tok_0_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT0_proj_decls[0], PS1Tok0), mk_int(ctx, 1));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 1, S1Critical_tok_0_and));
Z3_ast PS1Tok1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "PS1_Critical_tok1"), DT0SORT);
Z3_ast S1Critical_tok_1_and[1];
S1Critical_tok_1_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT0_proj_decls[0], PS1Tok1), mk_int(ctx, 2));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 1, S1Critical_tok_1_and));
property_and[1] = Z3_mk_not(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_true(ctx), Z3_mk_set_member(ctx, PS1Tok0, mk_unary_app(ctx, proj_decls[2], S1))), Z3_mk_set_member(ctx, PS1Tok1, mk_unary_app(ctx, proj_decls[2], S1))));
Z3_ast PS2Tok0 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "PS2_Critical_tok0"), DT0SORT);
Z3_ast S2Critical_tok_0_and[1];
S2Critical_tok_0_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT0_proj_decls[0], PS2Tok0), mk_int(ctx, 1));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 1, S2Critical_tok_0_and));
Z3_ast PS2Tok1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "PS2_Critical_tok1"), DT0SORT);
Z3_ast S2Critical_tok_1_and[1];
S2Critical_tok_1_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT0_proj_decls[0], PS2Tok1), mk_int(ctx, 2));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 1, S2Critical_tok_1_and));
property_and[2] = Z3_mk_not(ctx, mk_and(ctx, mk_and(ctx, Z3_mk_true(ctx), Z3_mk_set_member(ctx, PS2Tok0, mk_unary_app(ctx, proj_decls[2], S2))), Z3_mk_set_member(ctx, PS2Tok1, mk_unary_app(ctx, proj_decls[2], S2))));
Z3_ast PS3Tok0 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "PS3_Critical_tok0"), DT0SORT);
Z3_ast S3Critical_tok_0_and[1];
S3Critical_tok_0_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT0_proj_decls[0], PS3Tok0), mk_int(ctx, 1));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 1, S3Critical_tok_0_and));
Z3_ast PS3Tok1 = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, "PS3_Critical_tok1"), DT0SORT);
Z3_ast S3Critical_tok_1_and[1];
S3Critical_tok_1_and[0] = Z3_mk_eq(ctx, mk_unary_app(ctx, DT0_proj_decls[0], PS3Tok1), mk_int(ctx, 2));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 1, S3Critical_tok_1_and));
property_and[3] = mk_and(ctx, mk_and(ctx, Z3_mk_true(ctx), Z3_mk_set_member(ctx, PS3Tok0, mk_unary_app(ctx, proj_decls[2], S3))), Z3_mk_set_member(ctx, PS3Tok1, mk_unary_app(ctx, proj_decls[2], S3)));
Z3_assert_cnstr(ctx, Z3_mk_and(ctx, 4, property_and));

}


int main() {
#ifdef LOG_Z3_CALLS
    Z3_open_log("z3.log");
#endif

    		Z3_config  cfg;
        	Z3_context ctx;
         	cfg = Z3_mk_config();
           Z3_set_param_value(cfg, "MODEL", "true");
//           Z3_set_param_value(cfg, "AUTO_CONFIG", "false");
//           Z3_set_param_value(cfg, "MBQI", "false");
           ctx = mk_context_custom(cfg, error_handler);
		clock_t start = clock();
           nullInductionChecker(ctx);

    check(ctx, Z3_L_TRUE);
	printf("Time elapsed: %f\n", ((double)clock()-start)/CLOCKS_PER_SEC);

    printf("\nResult:\n%s\nEND OF Result\n", Z3_statistics_to_string(ctx));
//    printf("\nCONTEXT:\n%s\nEND OF CONTEXT\n", Z3_context_to_string(ctx));
    /* delete logical context */
     Z3_del_context(ctx);
     Z3_del_config(cfg);
    return 0;
}
