/**
 * AI API 연동 분석기 (2차 분석)
 * 패턴 매칭으로 해결되지 않는 에러를 Claude API로 분석한다.
 * API 키가 없으면 graceful하게 건너뛴다.
 */

import Anthropic from '@anthropic-ai/sdk';

export class AiAnalyzer {
  constructor() {
    this.client = null;
    this.enabled = false;

    const apiKey = process.env.ANTHROPIC_API_KEY;
    if (apiKey) {
      try {
        this.client = new Anthropic({ apiKey });
        this.enabled = true;
      } catch {
        this.enabled = false;
      }
    }
  }

  isEnabled() {
    return this.enabled;
  }

  /**
   * AI API로 에러를 분석한다.
   * @param {object} errorInfo - 파싱된 에러 정보
   * @returns {Promise<{ analysis: string, suggestedFix: string } | null>}
   */
  async analyze(errorInfo) {
    if (!this.enabled) return null;

    const prompt = this._buildPrompt(errorInfo);

    try {
      const response = await this.client.messages.create({
        model: 'claude-sonnet-4-5-20250929',
        max_tokens: 1024,
        messages: [
          {
            role: 'user',
            content: prompt
          }
        ]
      });

      const text = response.content[0]?.text || '';
      return this._parseResponse(text);
    } catch (err) {
      // API 호출 실패 시 null 반환 (패턴 매칭 결과만 사용)
      return null;
    }
  }

  _buildPrompt(errorInfo) {
    const stackTrace = errorInfo.stackTrace?.slice(0, 15).join('\n  ') || '(없음)';
    const appTrace = errorInfo.appStackTrace?.join('\n  ') || '(없음)';

    return `당신은 Spring Boot 에러 분석 전문가입니다.
아래 에러를 분석하고 수정 방법을 제안해주세요.

에러 타입: ${errorInfo.type}
에러 메시지: ${errorInfo.message || '(없음)'}
${errorInfo.rootCause ? `근본 원인: ${errorInfo.rootCause.type} - ${errorInfo.rootCause.message}` : ''}

애플리케이션 스택 트레이스:
  ${appTrace}

전체 스택 트레이스 (상위 15줄):
  ${stackTrace}

아래 형식으로 답변해주세요:

[분석]
(에러의 원인과 발생 맥락을 설명)

[수정 제안]
(구체적인 코드 수정 방법을 단계별로 제시)`;
  }

  _parseResponse(text) {
    const analysisMatch = text.match(/\[분석\]\s*([\s\S]*?)(?=\[수정 제안\]|$)/);
    const fixMatch = text.match(/\[수정 제안\]\s*([\s\S]*?)$/);

    return {
      analysis: analysisMatch ? analysisMatch[1].trim() : text.trim(),
      suggestedFix: fixMatch ? fixMatch[1].trim() : ''
    };
  }
}
