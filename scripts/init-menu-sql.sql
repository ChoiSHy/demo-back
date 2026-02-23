-- 1depth 메뉴 (상위 메뉴)
INSERT INTO tbl_menu (menu_name, menu_url, parent_id, menu_order, menu_depth, icon, use_yn, description, created_at, updated_at)
VALUES
  ('시스템 관리', null,   null, 1, 0, 'settings',   'Y', '시스템 관리 메뉴', SYSDATE(), SYSDATE()),
  ('사용자 관리', null,   null, 2, 0, 'people',     'Y', '사용자 관리 메뉴', SYSDATE(), SYSDATE()),
  ('콘텐츠 관리', null,   null, 3, 0, 'article',    'Y', '콘텐츠 관리 메뉴', SYSDATE(), SYSDATE());

-- 2depth 메뉴 (parent_id = 위에서 생성된 ID 기준)
INSERT INTO tbl_menu (menu_name, menu_url, parent_id, menu_order, menu_depth, icon, use_yn, description, created_at, updated_at)
VALUES
  ('메뉴 관리',     '/system/menu',      1, 1, 1, 'menu',         'Y', '메뉴 관리', SYSDATE(), SYSDATE()),
  ('권한 관리',     '/system/role',      1, 2, 1, 'lock',         'Y', '권한 관리', SYSDATE(), SYSDATE()),
  ('코드 관리',     '/system/code',      1, 3, 1, 'code',         'Y', '공통 코드 관리', SYSDATE(), SYSDATE()),
  ('회원 목록',     '/user/list',        2, 1, 1, 'list',         'Y', '회원 목록 조회', SYSDATE(), SYSDATE()),
  ('회원 등록',     '/user/register',    2, 2, 1, 'person_add',   'Y', '회원 등록', SYSDATE(), SYSDATE()),
  ('게시글 관리',   '/content/post',     3, 1, 1, 'edit_note',    'Y', '게시글 관리', SYSDATE(), SYSDATE()),
  ('카테고리 관리', '/content/category', 3, 2, 1, 'category',     'Y', '카테고리 관리', SYSDATE(), SYSDATE());
