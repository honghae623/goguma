import { useEffect, useMemo, useRef, useState } from 'react'
import './App.css'

const API = import.meta.env.VITE_API_URL ?? ''

const CATEGORIES = ['맛집', '카페', '쇼핑', '레시피', '기타']

export default function App() {
  const [file, setFile] = useState(null)
  const [category, setCategory] = useState('')
  const [extractedText, setExtractedText] = useState('')
  const [dragOver, setDragOver] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [ocrRunning, setOcrRunning] = useState(false)
  const ocrJob = useRef(0)
  const [error, setError] = useState('')
  const [captures, setCaptures] = useState([])
  const [filter, setFilter] = useState('')
  const inputRef = useRef(null)

  const [reload, setReload] = useState(0)
  const refresh = () => setReload((n) => n + 1)

  useEffect(() => {
    let cancelled = false
    const query = filter ? `?category=${encodeURIComponent(filter)}` : ''
    fetch(`${API}/api/captures${query}`)
      .then((res) => {
        if (!res.ok) throw new Error()
        return res.json()
      })
      .then((data) => {
        if (!cancelled) setCaptures(data)
      })
      .catch(() => {
        if (!cancelled) setError('목록을 불러오지 못했습니다. 백엔드(8080)가 실행 중인지 확인하세요.')
      })
    return () => {
      cancelled = true
    }
  }, [filter, reload])

  const preview = useMemo(() => (file ? URL.createObjectURL(file) : null), [file])

  useEffect(() => {
    return () => {
      if (preview) URL.revokeObjectURL(preview)
    }
  }, [preview])

  const pick = (f) => {
    if (!f) return
    if (!f.type.startsWith('image/')) {
      setError('이미지 파일만 선택할 수 있습니다.')
      return
    }
    setError('')
    setFile(f)
    runOcr(f)
  }

  const runOcr = async (f) => {
    const job = ++ocrJob.current
    setOcrRunning(true)
    setExtractedText('')
    try {
      const mod = await import('tesseract.js')
      const recognize = mod.recognize ?? mod.default.recognize
      const { data } = await recognize(f, 'kor+eng')
      if (job === ocrJob.current) setExtractedText(data.text.replace(/\s+/g, ' ').trim())
    } catch {
      if (job === ocrJob.current) setError('글자 인식에 실패했습니다. 텍스트를 직접 입력해 주세요.')
    } finally {
      if (job === ocrJob.current) setOcrRunning(false)
    }
  }

  const submit = async (e) => {
    e.preventDefault()
    if (!file) return
    const body = new FormData()
    body.append('file', file)
    if (category) body.append('category', category)
    if (extractedText.trim()) body.append('extractedText', extractedText.trim())

    setUploading(true)
    setError('')
    try {
      const res = await fetch(`${API}/api/captures/upload`, { method: 'POST', body })
      if (!res.ok) {
        throw new Error(res.status === 400 ? '지원하지 않는 이미지입니다.' : '업로드에 실패했습니다.')
      }
      setFile(null)
      setCategory('')
      setExtractedText('')
      refresh()
    } catch (err) {
      setError(err.message || '업로드에 실패했습니다.')
    } finally {
      setUploading(false)
    }
  }

  const remove = async (id) => {
    await fetch(`${API}/api/captures/${id}`, { method: 'DELETE' })
    refresh()
  }

  return (
    <main className="app">
      <h1>파타주 Partage</h1>
      <p className="sub">캡처 이미지를 올리면 카테고리를 자동으로 분류해요.</p>

      <form className="card upload" onSubmit={submit}>
        <div
          className={`drop${dragOver ? ' over' : ''}`}
          onClick={() => inputRef.current?.click()}
          onDragOver={(e) => {
            e.preventDefault()
            setDragOver(true)
          }}
          onDragLeave={() => setDragOver(false)}
          onDrop={(e) => {
            e.preventDefault()
            setDragOver(false)
            pick(e.dataTransfer.files[0])
          }}
        >
          {preview ? <img src={preview} alt="미리보기" /> : <span>이미지를 끌어다 놓거나 클릭해서 선택</span>}
          <input ref={inputRef} type="file" accept="image/*" onChange={(e) => pick(e.target.files[0])} />
        </div>

        <label>
          카테고리 (비우면 자동 분류)
          <select value={category} onChange={(e) => setCategory(e.target.value)}>
            <option value="">자동 분류</option>
            {CATEGORIES.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>
        </label>

        <label>
          이미지 속 텍스트 (자동 인식 · 수정 가능)
          <textarea
            value={extractedText}
            onChange={(e) => setExtractedText(e.target.value)}
            placeholder={ocrRunning ? '글자를 인식하는 중…' : '예: 스타벅스 아메리카노 라떼'}
          />
        </label>

        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={!file || uploading || ocrRunning}>
          {uploading ? '업로드 중…' : ocrRunning ? '글자 인식 중…' : '업로드'}
        </button>
      </form>

      <div className="list-head">
        <h2>내 캡처</h2>
        <div className="filters">
          {['', ...CATEGORIES].map((c) => (
            <button key={c} type="button" className={filter === c ? 'active' : ''} onClick={() => setFilter(c)}>
              {c || '전체'}
            </button>
          ))}
        </div>
      </div>

      {captures.length === 0 ? (
        <p className="empty">아직 캡처가 없어요.</p>
      ) : (
        <div className="grid">
          {captures.map((c) => (
            <article key={c.id} className="card item">
              <img src={`${API}${c.imageUrl}`} alt="" loading="lazy" />
              <div className="body">
                <span className="badge">{c.category}</span>
                {c.extractedText && <p className="text">{c.extractedText}</p>}
                <span className="time">{new Date(c.createdAt).toLocaleString('ko-KR')}</span>
                <button type="button" className="ghost" onClick={() => remove(c.id)}>
                  삭제
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </main>
  )
}
