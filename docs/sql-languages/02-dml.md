# DML：增删改（INSERT / UPDATE / DELETE）

> **DML 是四类语言里最危险的一类**——它真的改数据，而数据是删不回来的（至少不容易）。一个 `DELETE` / `UPDATE` 只要**漏了 WHERE**，一执行就是全表遭殃。本篇先把三个动词讲透，再给**护命三件事**：改之前先查、改的时候包事务、删空表想清楚用哪个。

## 1. 三个动词：放进来、换掉、拿走

```text
INSERT    往表里放一行（放新家具进来）
UPDATE    把某些行的某些列换成新值（把家具挪个位置/换个颜色）
DELETE    把某些行删掉（把家具搬出去）
```

和 DDL 的区别一句话：**DML 动的是"数据"（屋里家具），DDL 动的是"结构"（房子本身）**。动结构不可回滚，动数据可以用事务兜底——但前提是**你包了事务**。

## 2. 语法讲透：增、改、删

三个动词都围绕"行"——**加一行、改一行、删一行**。逐段看。

### 2.1 INSERT：加行

```text
INSERT INTO 表名 (列名, ...) VALUES (值, ...);
```

**语法三问：**
- **列名列表可省略**——省略 = 给所有列按顺序填值：`INSERT INTO student VALUES (1, '王小华', '高二(3)班');`。但一列对不上就报错，**规范起见，列名别省**。
- **一次能插多行**：用逗号并列多组值：

```sql
-- 一次插两行学生
INSERT INTO student(name, grade) VALUES ('王小华', '高二(3)班'), ('李小明', '高二(2)班');
```

- **`INSERT ... SELECT`：从别的表搬数据**（建临时表、迁移数据常用）：

```sql
-- 把成绩 ≥ 90 的学生复制到新表（新表要先建好）
INSERT INTO 优秀学生(name, grade)
SELECT name, grade FROM student WHERE student_id IN
  (SELECT student_id FROM student_course WHERE score >= 90);
```

**判断点：插入的是什么类型，想清楚。** 字符串要加引号 `'王小华'`，数字不加，日期用 `'2026-09-01'`。类型不符会报错，这是新手最常见的报错之一。

### 2.2 UPDATE：改行

```text
UPDATE 表名 SET 列1 = 新值, 列2 = 新值 ... WHERE 条件;
```

**要点 1：SET 可以同时改多列**（用逗号分隔）：
```sql
-- 换班 + 改备注，一次搞定
UPDATE student SET grade = '高三(1)班', phone = '139xxxx' WHERE student_id = 1;
```

**要点 2：不写 WHERE = 改全表。** 这是 UPDATE 的头号事故：
```sql
-- 本意：只改学号 1 的学生
UPDATE student SET grade = '高三(1)班' WHERE student_id = 1;
-- 漏了 WHERE：全班所有学生都变成"高三(1)班"
UPDATE student SET grade = '高三(1)班';
```

> **判断点 1：UPDATE/DELETE 不带 WHERE = 作用于全表。** 所以动手前先查——把 WHERE 拼进一条 SELECT 看命中哪些行，再原样抄回 UPDATE。**"先查后改"**，这个习惯救命的次数远比你想象的多。（对应 3.1 的完整流程）

### 2.3 DELETE：删行

```text
DELETE FROM 表名 WHERE 条件;
```

**要点：和 UPDATE 一样，不写 WHERE = 删全表**：
```sql
-- 本意：删学号 999 的学生
DELETE FROM student WHERE student_id = 999;
-- 漏了 WHERE：整张学生表被清空
DELETE FROM student;
```

**先查后改，对 DELETE 更要命：**
```sql
-- 第一步：查——确认即将命中的行
SELECT * FROM student WHERE student_id = 999;
-- 第二步：删——WHERE 一字不差地抄过来
DELETE FROM student WHERE student_id = 999;
```

> **为什么容易漏 WHERE？** 写的时候只记得"要删这条记录"的意图，忘了带全过滤条件。而 SQL 不像编程语言——**它没有"确认"弹窗，一条语句下去立刻生效**。

## 3. 护命三件事

语法都认识了，动手前再补三道保险。前两道其实在上一节已经埋下，这里讲深一点。

### 3.1 先查后改（WHERE 带了吗）

§2 里说过：UPDATE / DELETE 不带 WHERE = 全表遭殃。这里把"先查后改"的完整流程给全：

```text
SELECT 预览 → 确认命中行数 → 把 WHERE 原样抄进 UPDATE/DELETE
```

> 一句 SELECT 的代价几乎为零，一次误删的代价是全部。**先查后改，是 DML 的头号护命习惯。**

### 3.2 事务包了吗？（能回滚 vs 不能回滚）

**事务 = 把多条 SQL 捆成一笔"要么全成、要么全不成"的操作。** 就像转账：从 A 扣 100、给 B 加 100，这两步必须是一笔——任何一步失败，整个都不发生。

**语法就三个词，你要做的只有一个动作——"包起来"：**

```sql
START TRANSACTION;                          -- 开始：之后的改动先"存着"
UPDATE account SET balance = balance - 100 WHERE id = 1;   -- 第 1 笔改动
UPDATE account SET balance = balance + 100 WHERE id = 2;   -- 第 2 笔改动
COMMIT;                                     -- 确认：都成功，才真正落地
ROLLBACK;                                   -- 反悔：一切回到开始之前（出错了用）
```

**为什么"包起来"就能有后悔药？** 想想记账本：

```text
START TRANSACTION     开始记账：之后的改动先记在本子上，数据库本身没动
  UPDATE ...          改动写在本子上
  UPDATE ...          第二笔也写在本子上
COMMIT                确认：把本子上的改动一次性真正写进数据库
ROLLBACK              反悔：把本子撕掉，数据库还停在开始前
```

> **COMMIT 之前，改动都还没真的发生。** 在 COMMIT 前，其他连接看不到你的改动，你也能随时 ROLLBACK；一旦 COMMIT，改动就"定案"了。

**不包事务 vs 包了事务：**

```text
不包事务：改到一半出错，钱转出了、没转入 → 脏数据，回不去了
包了事务：出错就 ROLLBACK，一切回到开始前  → 有后悔药
```

**ROLLBACK 到底能救回什么？** 只救"事务内、还没 COMMIT 的改动"。**它救不了已经执行完的 DELETE/UPDATE**——想有后悔药，得在动手前就把条件造好，两个动作：

```text
① 把改动包进事务：删之前先 START TRANSACTION，出错就 ROLLBACK
   START TRANSACTION;                          -- 删前先开事务
   DELETE FROM student WHERE student_id = 999; -- 在事务里删
   ROLLBACK;                                   -- 不对？撕掉本子，一切还原

② 删前先备份：把要删的行复制到一张备份表，删错了还能捞回来
   CREATE TABLE student_bak AS                 -- 先备份（挑要删的行）
   SELECT * FROM student WHERE student_id = 999;
   DELETE FROM student WHERE student_id = 999;
   INSERT INTO student SELECT * FROM student_bak;   -- 后悔了？恢复回去
```

> 一句话：**rollback 救的是"事务内还没提交的改动"；已经执行的删除想恢复，靠的是动手前的备份。** 要么包事务，要么先备份——两条路都能让你后悔时有路可走。

**说到事务常被提起的 ACID，你不用深究。** 原子性（要么全成）、一致性（数据始终符合规则）、隔离性（并发不干扰）、持久性（提交不丢）——**这四样是数据库替你保证的，你唯一要做的事，就是"包事务"这一个动作**。剩下的，交给数据库；至于它内部用什么机制保证，那是数据库自己的事，不是你学习的功课。

**判断点：批量修改，看有没有事务包裹。** 一条 UPDATE 还好；**涉及多步、多表的修改，不包事务就是裸奔**。

### 3.3 删空表用哪个？（TRUNCATE vs DELETE）

| | DELETE | TRUNCATE |
|---|---|---|
| 删什么 | 按条件删**行**，可只删一部分 | 清空**整张表**，一次全没 |
| 要不要 WHERE | 要（不带就是全删） | 不需要，也没有 |
| 能不能回滚 | 包事务可回滚 | **大多数据库不可回滚** |
| 别的 | 不重置自增 id | **重置自增 id**（下一条又是 1） |

```sql
DELETE FROM student;    -- 清空，可回滚，但 id 接着往下长
TRUNCATE student;       -- 清空，不可回滚，id 重置回 1
```

> **判断点：看到"清空一张表"的需求，留意是不是给了 TRUNCATE——它在多数数据库里回不来。** 拿不准就选 DELETE + 事务。

### 隐藏陷阱：AUTOCOMMIT（自动提交）

很多连接默认开启 AUTOCOMMIT——**每条语句执行完立刻提交，你根本没机会 ROLLBACK**。也就是说：**不带 WHERE 的 DELETE，在 AUTOCOMMIT 下等于死刑立即执行，没有后悔药。**

```text
AUTOCOMMIT = ON（默认）：DELETE ...  → 立刻生效，回不去
AUTOCOMMIT = OFF：      DELETE ...  → 等你 COMMIT 才生效，可以 ROLLBACK
```

> 所以"包事务"要趁**语句执行前**就 START TRANSACTION，不是出事了才想起来。**先关掉/绕开 AUTOCOMMIT，再动数据的修改。**

## 4. 常见误区

| 误区 | 真相 |
|---|---|
| "SQL 删除有确认弹窗" | 没有。**一条 DELETE 下去立刻生效，没有二次确认** |
| "UPDATE 一定带了 WHERE" | 不一定。**DML 最容易漏 WHERE，先查后改** |
| "删错了可以随时回滚" | **ROLLBACK 只救事务内没提交的改动**。已执行的删除靠动手前的备份，不是回滚 |
| "清空表用 TRUNCATE 最干净" | 干净，但**大多不可回滚**；拿不准用 DELETE + 事务 |
| "UPDATE 一次只影响一条" | 不带 WHERE = 影响全表。**UPDATE 和 DELETE 一样危险** |

---

**小结**：DML 的语法十分钟学会，但**保命靠三个习惯——先查后改、包事务、删空表选对词**。这三条，也是动数据前最值得反复提醒自己的。下一步看改结构的 [03 改结构 DDL](./03-ddl.md)——那是"不可回滚"的重灾区。
