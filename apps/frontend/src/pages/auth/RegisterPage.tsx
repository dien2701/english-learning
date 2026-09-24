import React, { useCallback, useEffect, useRef, useState } from "react";
import { App, Checkbox, Form, Input } from "antd";
import { useTranslation } from "react-i18next";
import { passwordRule } from '../../shared/validation/password';
import { Link, useNavigate } from "react-router-dom";

import AuthLayout from "../../components/auth/AuthLayout";
import PasswordStrength from "../../components/auth/PasswordStrength";
import { Button } from "../../components/ui/Button";
import { useAuth } from "../../contexts/AuthContext";
import { useApiError } from "../../hooks/useApiError";
import { authService } from "../../services/authService";

interface RegisterForm {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  code: string;
  acceptTerms: boolean;
}

/** Kết quả hỏi server xem email đã có tài khoản chưa. */
type EmailCheck =
  { state: "idle" } | { state: "checking" } | { state: "available" };

/** Khớp khoảng chờ giữa hai lần gửi mã ở backend. */
const RESEND_COOLDOWN_SECONDS = 60;

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const RegisterPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [password, setPassword] = useState("");
  const [emailCheck, setEmailCheck] = useState<EmailCheck>({ state: "idle" });
  const [form] = Form.useForm<RegisterForm>();
  const { message } = App.useApp();
  const { t, i18n } = useTranslation();
  /* Bước 1 chỉ nhập email và nhận mã; bước 2 mới điền phần còn lại. */
  const [sentTo, setSentTo] = useState<string | null>(null);
  const [sending, setSending] = useState(false);
  const [cooldown, setCooldown] = useState(0);
  const navigate = useNavigate();
  const { register } = useAuth();
  const { describe, applyTo } = useApiError();

  /* Người dùng có thể rời ô email nhiều lần; chỉ lời gọi cuối cùng được
     phép ghi kết quả, nếu không một phản hồi cũ về muộn sẽ đè lên. */
  const checkSeq = useRef(0);

  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = window.setTimeout(
      () => setCooldown((value) => value - 1),
      1000,
    );
    return () => window.clearTimeout(timer);
  }, [cooldown]);

  const sendCode = async () => {
    let email: string;
    try {
      ({ email } = await form.validateFields(["email"]));
    } catch {
      return;
    }
    email = email.trim();
    setSending(true);
    try {
      await authService.sendRegisterCode(email, i18n.language);
      setSentTo(email);
      setCooldown(RESEND_COOLDOWN_SECONDS);
      message.success(t("auth.verifyCodeSent", { email }));
    } catch (error) {
      applyTo(form, error);
      message.error(describe(error, "errors.emailSendFailed"));
    } finally {
      setSending(false);
    }
  };

  const changeEmail = () => {
    setSentTo(null);
    setCooldown(0);
    form.setFieldValue("code", undefined);
  };

  const onFinish = async (values: RegisterForm) => {
    setLoading(true);
    try {
      await register({
        fullName: values.fullName,
        email: sentTo ?? values.email,
        code: values.code,
        password: values.password,
        confirmPassword: values.confirmPassword,
      });
      message.success(t("auth.registerSuccess"));
      navigate("/dashboard", { replace: true });
    } catch (error) {
      // Đưa lỗi về đúng ô nhập thay vì chỉ hiện một thông báo chung.
      applyTo(form, error);
      message.error(describe(error, "auth.registerFailed"));
    } finally {
      setLoading(false);
    }
  };

  /**
   * Hỏi server ngay khi rời ô email. Biết email đã có tài khoản từ lúc này
   * thì người dùng sửa luôn, thay vì điền hết form rồi mới nhận lỗi.
   */
  const handleEmailBlur = useCallback(
    async (event: React.FocusEvent<HTMLInputElement>) => {
      const email = event.target.value.trim();

      if (!EMAIL_PATTERN.test(email)) {
        setEmailCheck({ state: "idle" });
        return;
      }

      const seq = ++checkSeq.current;
      setEmailCheck({ state: "checking" });

      try {
        const { available } = await authService.checkEmail(email);
        if (seq !== checkSeq.current) return;

        if (available) {
          setEmailCheck({ state: "available" });
        } else {
          setEmailCheck({ state: "idle" });
          form.setFields([{ name: "email", errors: [t("errors.emailTaken")] }]);
        }
      } catch {
        /* Mạng hỏng thì im lặng bỏ qua: đây chỉ là kiểm tra sớm cho tiện,
           lúc bấm nút đăng ký server vẫn kiểm tra lại lần nữa. */
        if (seq === checkSeq.current) setEmailCheck({ state: "idle" });
      }
    },
    [form, t],
  );

  return (
    <AuthLayout
      title={t("auth.createAccount")}
      subtitle={
        sentTo
          ? t("auth.verifyCodeSent", { email: sentTo })
          : t("auth.registerSubtitle")
      }
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        requiredMark={false}
        size="large"
        /* Kiểm tra ngay khi rời ô, rồi mới theo từng phím gõ — người dùng
           thấy lỗi sớm nhưng không bị mắng ngay từ ký tự đầu tiên. */
        validateTrigger={["onBlur", "onChange"]}
        initialValues={{ acceptTerms: false }}
      >
        <Form.Item
          label={t("auth.email")}
          name="email"
          rules={[
            { required: true, message: t("auth.validation.emailRequired") },
            { type: "email", message: t("auth.validation.emailFormat") },
          ]}
          validateStatus={
            emailCheck.state === "checking" ? "validating" : undefined
          }
          hasFeedback={emailCheck.state === "checking"}
          extra={
            sentTo ? (
              <button
                type="button"
                onClick={changeEmail}
                className="text-[13px] font-bold text-accent hover:underline"
              >
                {t("auth.changeEmail")}
              </button>
            ) : emailCheck.state === "available" ? (
              <span className="inline-flex items-center gap-1 text-accent">
                <span
                  aria-hidden="true"
                  className="material-symbols-outlined text-[15px]"
                >
                  check_circle
                </span>
                {t("auth.emailAvailable")}
              </span>
            ) : emailCheck.state === "checking" ? (
              t("auth.emailChecking")
            ) : undefined
          }
        >
          <Input
            placeholder="name@example.com"
            autoComplete="email"
            inputMode="email"
            onBlur={handleEmailBlur}
            onChange={() => setEmailCheck({ state: "idle" })}
            disabled={sentTo !== null}
          />
        </Form.Item>

        {sentTo === null ? (
          <>
            <p className="mb-4 text-[13.5px] text-ink-muted">
              {t("auth.verifyStepHint")}
            </p>
            <Button
              type="button"
              size="lg"
              loading={sending}
              block
              onClick={sendCode}
            >
              {t("auth.sendVerifyCode")}
            </Button>
            <div className="mt-4 text-center">
              <span className="text-[13.5px] text-ink-muted">
                {t("auth.hasAccount")}{" "}
                <Link
                  to="/login"
                  className="text-[13.5px] font-bold text-accent hover:underline"
                >
                  {t("auth.login")}
                </Link>
              </span>
            </div>
          </>
        ) : (
          <>
            <Form.Item
              label={t("auth.verifyCode")}
              name="code"
              rules={[
                { required: true, message: t("auth.validation.codeRequired") },
              ]}
              extra={
                <button
                  type="button"
                  onClick={sendCode}
                  disabled={cooldown > 0 || sending}
                  className="text-[13px] font-bold text-accent hover:underline disabled:cursor-not-allowed disabled:text-ink-muted disabled:no-underline"
                >
                  {cooldown > 0
                    ? t("auth.resendIn", { seconds: cooldown })
                    : t("auth.resendCode")}
                </button>
              }
            >
              <Input
                placeholder="123456"
                inputMode="numeric"
                autoComplete="one-time-code"
                maxLength={6}
              />
            </Form.Item>

            <Form.Item
              label={t("auth.fullName")}
              name="fullName"
              rules={[
                { required: true, message: t("auth.validation.nameRequired") },
                { min: 2, message: t("auth.validation.nameShort") },
              ]}
            >
              <Input
                placeholder={t("auth.namePlaceholder")}
                autoComplete="name"
              />
            </Form.Item>

            <Form.Item
              label={t("auth.password")}
              name="password"
              rules={[
                {
                  required: true,
                  message: t("auth.validation.passwordRequired"),
                },
                passwordRule(t),
              ]}
              extra={t("auth.passwordHint")}
            >
              <Input.Password
                placeholder="••••••••"
                autoComplete="new-password"
                onChange={(event) => setPassword(event.target.value)}
              />
            </Form.Item>

            <PasswordStrength password={password} className="-mt-3 mb-5" />

            <Form.Item
              label={t("auth.confirmPassword")}
              name="confirmPassword"
              dependencies={["password"]}
              rules={[
                {
                  required: true,
                  message: t("auth.validation.confirmRequired"),
                },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue("password") === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(
                      new Error(t("auth.validation.confirmMismatch")),
                    );
                  },
                }),
              ]}
            >
              <Input.Password
                placeholder="••••••••"
                autoComplete="new-password"
              />
            </Form.Item>

            <Form.Item
              name="acceptTerms"
              valuePropName="checked"
              rules={[
                {
                  validator: (_, value: boolean) =>
                    value
                      ? Promise.resolve()
                      : Promise.reject(
                          new Error(t("auth.validation.termsRequired")),
                        ),
                },
              ]}
            >
              <Checkbox>
                {/* Tên hai văn bản để chữ thường chứ không phải link: dự án
                chưa có trang Điều khoản và Chính sách, mà link dẫn tới
                trang 404 còn tệ hơn là không có link. Khi hai trang đó
                ra đời thì bọc lại bằng <Link>. */}
                <span className="text-[13.5px] text-ink">
                  {t("auth.termsPrefix")}{" "}
                  <strong className="font-bold text-accent">
                    {t("auth.termsLink")}
                  </strong>{" "}
                  {t("auth.and")}{" "}
                  <strong className="font-bold text-accent">
                    {t("auth.privacyLink")}
                  </strong>
                </span>
              </Checkbox>
            </Form.Item>

            <Button type="submit" size="lg" loading={loading} block>
              {t("auth.createAccount")}
            </Button>
            <div className="mt-4 text-center">
              <span className="text-[13.5px] text-ink-muted">
                {t("auth.hasAccount")}{" "}
                <Link
                  to="/login"
                  className="text-[13.5px] font-bold text-accent hover:underline"
                >
                  {t("auth.login")}
                </Link>
              </span>
            </div>
          </>
        )}
      </Form>
    </AuthLayout>
  );
};

export default RegisterPage;
