# DDL：改结构（CREATE / ALTER / DROP）

> **DDL 是"改房子结构"，而且大多不可回滚**——DROP 一张表，表和数据一起没了。所以本篇的重点不是背 CREATE 的写法，而是**动手前想清楚三件事**：会不会破坏已有数据、索引是不是乱建、DROP 之前想没想清楚。

## 1. 语法讲透：建、改、删

### 1.1 CREATE：建数据库

动手建表之前，得先有个"房子"——**数据库（database）是装着所有表的容器**。建库和删库：

```sql
-- 建库（数据库名，一般带编码设置）
CREATE DATABASE edu_system
    DEFAULT CHARACTER SET utf8mb4;

-- 删库（⚠️ 里面所有表和数据一起没，比 DROP TABLE 更狠）
DROP DATABASE edu_system;

-- 切换使用哪个库（后面所有操作都落在它里面）
USE edu_system;
```

```text
CREATE DATABASE 建库（盖整栋楼）
USE 库名          进到这栋楼里干活
DROP DATABASE    拆楼（整栋楼的所有表和数据一起没）
```

> **`USE` 不是 DDL**，它只是"把当前会话指到哪个库"，但它是建库后第一件要做的事。**建库之后不 `USE`，后面的建表会不知道该建到哪。**

### 1.2 CREATE：建表

建表就是"**给一张表定好列、列的类型、约束**"。逐段读：

```sql
CREATE TABLE teacher (
    teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT,   -- 主键，自动递增（自增）
    name       VARCHAR(50)  NOT NULL,               -- 变长字符串，最多 50 字符，不能为空
    phone      VARCHAR(20)                          -- 变长字符串，允许为空
);
```

**四个最常见的关键字：**

```text
VARCHAR(n)    变长字符串，最多 n 个字符 —— 名字、电话这类文本
BIGINT        长整型（8 字节整数）      —— 主键、id 这类
DECIMAL(m,d)  精确小数                  —— 钱、成绩这类要精确的
DATE / DATETIME  日期 / 日期时间        —— 生日、创建时间

PRIMARY KEY   主键：这一列唯一标识一行，不能重复、不能为空
AUTO_INCREMENT  自增：插入时不填，数据库自动 +1（通常配主键）
NOT NULL      不能为空：这一列必须有值
UNIQUE        唯一：这一列的值不能重复（手机号这种）
DEFAULT 值    不填时用这个默认值
```

**类型该怎么选？** 一句话原则——**数字用数字类型，文本用字符串，钱和精确值用 DECIMAL，时间用日期类型。** 把数字存进字符串、把价格存成 FLOAT，都是以后要踩的坑。

### 1.3 ALTER：改结构

结构建好还能改。**四种操作，全是对列下手：**

```text
ALTER TABLE 表名
  ADD    COLUMN 列名 类型        加一列
  DROP   COLUMN 列名             删一列（⚠️ 这一列的数据一起没）
  MODIFY COLUMN 列名 新类型      改列的"类型/约束"
  CHANGE COLUMN 旧名 新名 新类型  改列名（顺带能改类型）
```

```sql
-- 加一列：给老师加职称
ALTER TABLE teacher ADD COLUMN title VARCHAR(20);

-- 删一列：把刚加的职称删掉（title 这一列的数据一起没）
ALTER TABLE teacher DROP COLUMN title;

-- 改类型：把 phone 从 20 个字符放宽到 30
ALTER TABLE teacher MODIFY COLUMN phone VARCHAR(30);

-- 改列名 + 类型：phone → contact_phone，顺带改成 30 字符
ALTER TABLE teacher CHANGE COLUMN phone contact_phone VARCHAR(30);
```

> **ALTER 动的是"结构"，但会波及"数据"。** 改类型的代价尤其值得记：`VARCHAR(20)` 放宽到 `VARCHAR(30)` 很安全（只是允许更长）；反过来从 `VARCHAR(30)` 收紧到 `VARCHAR(20)`，**超过 20 个字符的老数据会报错或截断**——不是随便收紧的。改约束也一样（见下节"破坏数据"）。

### 1.4 DROP：删表

```sql
-- 删表（⚠️ 表和数据一起没，别手滑）
DROP TABLE teacher;
```

> **DDL 为什么大多不可回滚？** DML 可以用事务兜底（先记账、再提交），但 DDL 在多数数据库里一执行就立即生效、直接改磁盘上的结构——DROP 一张表，表和里面的数据瞬间没了，没有"本子"可撕。**所以 DDL 动手前，只能靠"想清楚"来兜底。**

## 2. 动手前想清楚三件事

### 第一件：会不会破坏已有数据？

DDL 动的是结构，但结构变更会**波及里面的数据**。最常见的坑：

```sql
-- 想"给 phone 加个 NOT NULL 约束，更规范"
ALTER TABLE teacher MODIFY COLUMN phone VARCHAR(20) NOT NULL;
-- 问题：如果表里已经存在 phone 为 NULL 的老数据 → 这一步直接失败
```

```text
对空表 / 新表加约束：没问题
对有数据的表加约束：老数据不满足 → 操作报错，或要先用 UPDATE 把脏数据补上
```

> **动手前先想："表里现有数据会不会被这条 DDL 难住？"** 拿不准就先把表里数据 SELECT 出来看看，或者先备份。

### 第二件：索引是不是乱建？

建表时很容易**恨不得每个字段都加索引**。索引不是越多越好：

```text
索引的代价：每次增删改，都要同步更新索引
索引多了：查询变快，写入变慢，磁盘占用变大

所以：只为"经常被 WHERE / JOIN / ORDER BY 用到的列"建索引
```

```sql
-- ❌ 常见错误：主键建了，name/grade/phone 全加索引
CREATE TABLE student (
    student_id BIGINT PRIMARY KEY,
    name       VARCHAR(50),
    grade      VARCHAR(20),
    phone      VARCHAR(20),
    INDEX idx_name  (name),
    INDEX idx_grade (grade),
    INDEX idx_phone (phone)
);
-- ✅ 合理：只给"查询里真正用 WHERE/JOIN 过滤的列"建索引
CREATE TABLE student (
    student_id BIGINT PRIMARY KEY,
    name       VARCHAR(50),
    grade      VARCHAR(20),
    phone      VARCHAR(20),
    INDEX idx_name (name)   -- 按名字查（见 01-dql 的 EXPLAIN 例子）
);
```

> **先问"这张表经常按哪些列查"，再决定索引；字段越多，越要砍。** 索引如何让查询变快，见 [01 查询 DQL](./01-dql.md) 的索引一节。

### 2.2.1 索引的增删，也是 DDL

索引是"表结构的一部分，不是数据"，所以加索引、删索引都属于 DDL。**给一张已存在的表加索引，其实就一行：**

```sql
-- 建索引（独立命令）
CREATE INDEX idx_name ON student(name);

-- 等价写法：用 ALTER 给表加索引（效果一样）
ALTER TABLE student ADD INDEX idx_name (name);

-- 删索引
DROP INDEX idx_name ON student;
```

> **发现慢查询 → 建索引，是 DQL 和 DDL 的交接点。** 01-dql 里用 `EXPLAIN` 看到 `type: ALL`（全表扫描），多半就是 `WHERE` 的列没索引；回到本篇，一行 `CREATE INDEX` 就能解决。建完再用 `EXPLAIN` 看，`type` 从 `ALL` 变成 `ref`，就说明这行建对了。

### 2.2.2 这一行到底值不值？

`INDEX idx_name (name)` 只有一行，但**这一行背后，是数据库一直在替你干活**：

```text
建索引时：  给 name 列建一本"小册子"（索引结构），把表里现有所有 name 值抄进去，记下每行在哪
之后每次增删改：数据库自动维护这本小册子 —— 加一行、改一页、删一条
```

> 难的部分（怎么组织、怎么找最快、怎么维护）**数据库全包了，留给你的一行是刻意变简单**。但也正因如此，**这一行不廉价**——每多一个索引，每次写操作就多维护一本小册子。所以：**一行 SQL 不贵，乱建一堆索引才贵。**

**判断索引值不值，就一句话：** 建完后用 `EXPLAIN` 看 `type` 有没有从 `ALL` 变成 `ref` 之类——变了，这行建对了；还是 `ALL`，说明没走对，白建。

### 第三件：DROP 之前想清楚？

```text
DROP TABLE teacher;
  ├── 表结构没了
  ├── 表里所有数据没了（不可回滚！）
  └── 别的表的外键指向它 → 可能连坐报错
```

> **DROP 是最不可逆的操作。** 执行前问自己三句——这张表还要吗？数据要不要备份？有没有别的表引用它？**别因为"看起来没用了"就顺手删。**

## 3. 和前面章节的衔接

DDL 的 CREATE 就是"把设计变成现实"的那一步：

```text
[数据库设计](../database-design.md)  决定该有哪些表、外键放哪   ← 设计
[数据库范式](../database-normalization.md)  验证设计质量       ← 检查
[本篇 DDL]                    建库 + 建表，落成 CREATE 语句     ← 落地
```

> 落地顺序：**先 `CREATE DATABASE` 建库 → `USE` 进库 → 再 `CREATE TABLE` 建表**。库在前，表在后。

建表时对齐三件事：

```text
① 主键建了吗？       每个实体表都要有主键（student_id 这种）
② 外键对吗？         一对多把外键放在"多"的那边；多对多用中间表
③ 范式过了吗？       有没有故意冗余的字段？是不是该拆表？
```

> 一条路走完：**设计（识别实体和关系）→ 范式验证 → CREATE 落地。** 前两步想清楚，第三步的 CREATE 写出来后，**按上面的三件事过一遍**。

## 4. 常见误区

| 误区 | 真相 |
|---|---|
| "索引越多越好" | **索引有代价：写变慢、占空间**。只给常查的列建 |
| "DDL 可以回滚" | **大多 DDL 不可回滚**，DROP 尤其致命 |
| "给有数据的表加约束没事" | **老数据不满足新约束会报错**。先看数据再动手 |
| "类型可以随便收窄" | **从 VARCHAR(30) 收到 VARCHAR(20)，超长的老数据会报错或截断** |
| "DROP 只是删张表" | **连数据一起没**，还有外键连坐风险 |

---

**小结**：DDL 的关键词是**"不可回滚"**。建表、改表、删表都可以，但**破坏数据、乱建索引、手滑 DROP**这三个坑，动手前一条条过。下一步看最后一块拼图 [04 权限 DCL](./04-dcl.md)——数据库安全从管好钥匙开始。
