DROP TABLE IF EXISTS public.users CASCADE;

CREATE TABLE public.users
(
    id        UUID NOT NULL,
    username  VARCHAR,
    firstname VARCHAR,
    lastname  VARCHAR,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE public.users
    OWNER TO postgres;

DROP TABLE IF EXISTS public.documents CASCADE;

CREATE TABLE public.documents
(
    id          UUID    NOT NULL,
    document_id VARCHAR NOT NULL,
    CONSTRAINT documents_pkey PRIMARY KEY (id)
);

ALTER TABLE public.documents
    OWNER TO postgres;

DROP TABLE IF EXISTS public.user_permissions CASCADE;

CREATE TABLE public.user_permissions
(
    user_id            UUID NOT NULL,
    document_id        UUID NOT NULL,
    user_permission_id UUID NOT NULL,
    permission_type    VARCHAR,
    CONSTRAINT document_fk FOREIGN KEY (document_id)
        REFERENCES public.documents (id),
    CONSTRAINT user_fk FOREIGN KEY (user_id)
        REFERENCES public.users (id)
);

ALTER TABLE public.user_permissions
    OWNER TO postgres;

CREATE INDEX fki_user_fk
    ON public.user_permissions (user_id);

CREATE INDEX fki_document_fk
    ON public.user_permissions (document_id);