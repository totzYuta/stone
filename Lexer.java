package stone;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
  // \s*((//.*)|( pat1 )|( pat2 )| pat3)? という構成
  // 空白, コメント, pat1(整数リテラル), pat2(文字列リテラル), pat3(識別子) のどれかにマッチする正規表現
  public static String regexPat
    = "\\s*((//.*)|([0-9]+)|(\"(\\\\"|\\\\\\\\|\\\\n|[^\"]*\")" + "|[A-Z_a-z][A-Z_a-z_0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct})?";
  // 正規表現をPatternオブジェクトにコンパイルする->Matcherオブジェクトに渡す
  private Pattern pattern = Pattern.compile(regexPat);
  // readLine()により取り出したトークンをいったん保存するキュー
  // readメソッドで取り出された時に削除
  private ArrayList<Token> queue = new ArrayList<Token>();
  private boolean hasMore;
  private LineNumberReader reader;

  public Lexer(Reader r) {
    hasMore = true;
    reader = new LineNumberReader(r);
  }

  // メインになるメソッドひとつめ
  // 呼ばれるたびにソースコードの先頭からトークンを1つずつ順番に取り出して返す
  public Token read() throws ParseException {
    if (fillQueue(0))
      return queue.remove(0);
    else 
      return Token.EOF;
  }

  // メインになるメソッドふたつめ
  // 先読みをするためのメソッド
  // backtrackに対応するために必要
  // readもないと覚えなくていいtokenも覚えないといけなくてメモリ消費量が増える
  public Token peek(int i) throws ParseException {
    if (fillQueue(i))
      return queue.get(i);
    else 
      return Token.EOF;
  }

  private boolean fillQueue(int i) throws ParseException {
    while (i >= queue.size())
      if (hasMore)
        readLine();
      else    
        return false;
    return true;
  }

  protected void readLine() throws ParseException {
    String line;
    try {
      line = reader.readLine();
    } catch (IOException e) {
      throw new ParseException(e);
    }
    if (line == null) {
      hasMore = false;
      return;
    }
    int lineNo = reader.getLineNumber();
    // 正規表現からMatcherオブジェクトを生成
    Matcher matcher = pattern.matcher(line);
    matcher.useTransparentBounds(true).useAnchoringBounds(false);
    int pos = 0;
    int endPos = line.length();
    // 行の中にトークンがなくなるまで繰り返す
    while (pos < endPos) {
      // 照合範囲を狭める
      matcher.region(pos, endPos);
      // LookingAt()メソッドで照合範囲の先頭から
      // 正規表現に一致する部分を調べる
      if (matcher.LookingAt()) {
        addToken(lineNo, matcher);
        // 一致した範囲の末尾の位置でposを更新
        pos = matcher.end();
      }
      else
        throw new ParseException("bad token at line " + lineNo);
    }
    queue.add(new IdToken(lineNo, Token.EOL));
  }

  protected void addToken(int lineNo, Matcher matcher) {
    String m = matcher.group(1);
    if (m != null) // if not a space
      if (matcher.group(2) == null) { // if not a comment
        Token token;
        if (matcher.grounp(3) != null)
          token = new NumToken(lineNo, Integer.parseInt(m));
        else if (matcher.group(4) != null)
          token = new StrToken(lineNo, toStringLiteral(m));
        else 
          token = new IdToken(lineNo, m);
        queue.add(token);
      }
  }

  protected String toStringLiteral(String s) {
    StringBuilder sb = new StringBuilder();
    int len = s.length() - 1;
    for (int i = 1; i < len; i++) {
      char c = s.charAt(i);
      if (c == '\\' && i + 1 < len) {
        int c2 = s.charAt(i+1);
        if (c2 == '"' || c2 == '\\')
          c = s.charAt(++i);
        else if (c2=='n') {
          ++i;
          c = '\n';
        }
      }
      sb.append(c);
    }
    return sb.toString(c);
  }

  protected static class NumToken extends Token {
    private int value;

    protected NumToken(int line, int v) {
      super(line);
      value = v;
    }
    public boolean isNumber() { return true; }
    public String getText() { return Integer.toString(value); }
    public int getNumber() { return value; }
  }

  protected static class IdToken extends Token {
    private String text;
    protected IdToken(int line, String id) {
      super(line);
      text = id;
    }
    public boolean isIdentifier() { return true; }
    public String getText() { return text; }
  }

  protected static class StrToken extends Token {
    private String literal;
    StrToken(int line, String str) {
      super(line);
      literal = str;
    }
    public boolean isString() { return true; }
    public String getText() { return literal; }
  }
}
