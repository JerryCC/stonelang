package tlang;

import static tlang.Parser.*;

import java.util.HashSet;

import tlang.Parser.Operators;
import tlang.ast.ASTree;
import tlang.ast.BinaryExpr;
import tlang.ast.BlockStmnt;
import tlang.ast.IfStmnt;
import tlang.ast.Name;
import tlang.ast.NegativeExpr;
import tlang.ast.NumberLiteral;
import tlang.ast.NullStmnt;
import tlang.ast.PrimaryExpr;
import tlang.ast.StringLiteral;
import tlang.ast.WhenStmnt;
import tlang.ast.WhileStmnt;

public class BasicParser {
	HashSet<String> reserved = new HashSet<String> ();
	Operators operators = new Operators();
	Parser expr0 = rule();

	/**
	 * primary : either a primary expression class, or
	 *	 Primary "(" expr ")" | number | identifier | string
	 */
	
	Parser primary = rule(PrimaryExpr.class)
					.or(rule().sep("(").ast(expr0).sep(")"),
						rule().number(NumberLiteral.class),
						rule().identifier(Name.class, reserved),
						rule().string(StringLiteral.class));
	
	/**
	 * factor: a negative primary expression ( when it is a number ), or a primary expression
	 */
	
	Parser factor = rule().or(rule(NegativeExpr.class).sep("-").ast(primary), primary);
	
	/**
	 * expression : factor or a operator + factor
	 */
	Parser expr = expr0.expression(BinaryExpr.class, factor, operators);
	
	
	Parser statement0 =  rule();
	
	/**
	 * block    : "{" [ statement ] {(";" | EOL) [ statement]} "}"
	 */
	Parser block = rule(BlockStmnt.class)
			.sep("{").option(statement0)
			.repeat(rule().sep(";", Token.EOL).option(statement0))
			.sep("}");
	
	/**
	 * simple   : expr
	 */
	Parser simple = rule(PrimaryExpr.class).ast(expr);
	
	/**
	 * statement : "if" expr block [ "else" block ]
	 *           | "while" expr block
	 *           | "when" expr block
	 *           | simple  
	 */
	Parser statement = statement0.or(
			rule(IfStmnt.class).sep("if").ast(expr).ast(block)
						.option(rule().sep("else").ast(block)),
			rule(WhenStmnt.class).sep("when").ast(expr).ast(block),
			rule(WhileStmnt.class).sep("while").ast(expr).ast(block),
			simple);
	
	/**
	 * program   : [ statement ] (";" | EOL)
	 */
	Parser program = rule().or(statement, rule(NullStmnt.class))
						.sep(";", Token.EOL);
	
	/**
	 * 
	 */
	public BasicParser() {
		reserved.add(";");
		reserved.add("}");
		reserved.add(Token.EOL);
		
		operators.add("=", 1, Operators.RIGHT);
		operators.add("==", 2, Operators.LEFT);
		operators.add(">", 2, Operators.LEFT);
		operators.add("<", 2, Operators.LEFT);
		operators.add("+", 3, Operators.LEFT);
		operators.add("-", 3, Operators.LEFT);
		operators.add("*", 4, Operators.LEFT);
		operators.add("/", 4, Operators.LEFT);
		operators.add("%", 4, Operators.LEFT);
	}
	
	/**
	 * 
	 * @param lexer
	 * @return
	 * @throws ParseException
	 */
	public ASTree parse(Lexer lexer) throws ParseException {
		return program.parse(lexer);
	}
}
